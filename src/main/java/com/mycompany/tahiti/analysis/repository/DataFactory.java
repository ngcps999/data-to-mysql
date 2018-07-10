package com.mycompany.tahiti.analysis.repository;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mycompany.tahiti.analysis.jena.JenaLibrary;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Okio;
import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
public class DataFactory {
    @Autowired
    JenaLibrary jenaLibrary;

    @Autowired
    MongoCaseRepo mongoCaseRepo;

    private static final Logger LOG = Logger.getLogger(DataFactory.class);
    @Value("${miami.person.uri}")
    private String personURI;
    Gson gson = new Gson();
    JsonParser jsonParser = new JsonParser();
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Integer.MAX_VALUE, TimeUnit.MILLISECONDS)
            .readTimeout(Integer.MAX_VALUE, TimeUnit.MILLISECONDS)
            .build();
    // This is only for cache, not full data
    //For BI overall
    private Integer personCountCache = null;
    private Integer biluCountCache = null;
    private Integer caseCountCache = null;

    //subjectId, personBiluCount
    private Map<String, Integer> personBiluCount = null;

    //tag, tagBiluCount
    private Map<String, Integer> tagBiluCount = null;

    //subjectId, Person
    //Only contains person subjectId + person name + identity + bilu subjectId + case subjectId
    private Map<String, Person> personRelationCache = new HashMap<>();

    // key: phone numbet, value list of case subjectId
    private Map<String, List<String>> phoneCaseRelationCache = new HashMap<>();

    // subjectId, Case
    private Map<String, Case> caseCache = new HashMap<>();

    // subjectId， Bilu
    private Map<String, Bilu> biluCache = new HashMap<>();

    // subjectId, Person
    private Map<String, Person> personCache = new HashMap<>();

    // This is all cases with SimpleCase
    // subjectId, SimpleCase
    private Map<String, CaseBaseInfo> allSimpleCases = new HashMap<>();

    // data from mongodb aj_basic
    // ajbh, CaseMongo
    private Map<String, CaseMongo> caseMongoMap = new HashMap<>();

    private Map<String, String> identitySubIdMap = new HashMap<>();

    public boolean clear() {
        caseCache.clear();
        biluCache.clear();
        personCache.clear();
        allSimpleCases.clear();
        personRelationCache.clear();
        phoneCaseRelationCache.clear();
        personBiluCount = null;
        tagBiluCount = null;
        personCountCache = null;
        biluCountCache = null;
        caseCountCache = null;

        getAllCaseBaseInfo();
        getPersonRelation();
        getPhoneCaseRelationCache();
        return true;
    }

    //!!! Note: Will put bilu subjectId and case subject Id in Person
    public Map<String, Person> getPersonRelation() {
        if (personRelationCache.size() > 0) return personRelationCache;
        try {
            jenaLibrary.openReadTransaction();
            Map<String, Person> personRelation = new HashMap<>();

            //get all person subjectId
            Model model = jenaLibrary.getRuntimeModel();
            if (model == null)
                return new HashMap<>();

            Iterator<Statement> iter = jenaLibrary.getStatementsByEntityType(model, "common:person.person");
            List<String> personResourceList = new ArrayList<>();
            while (iter.hasNext()) {
                String personSubjectId = iter.next().getSubject().toString();
                personResourceList.add(personSubjectId);
                Person person = new Person();
                person.setSubjectId(personSubjectId);
                personRelation.put(personSubjectId, person);
            }

            HashSet<String> personSet = new HashSet<>(personResourceList);

            //get all person subjectId to biluId
            Iterator<Statement> personBiluIter = jenaLibrary.getStatementsByBatchPO(model, "gongan:gongan.bilu.entity", personSet);
            List<String> biluResoursceList = new ArrayList<>();
            while (personBiluIter.hasNext()) {
                Statement statement = personBiluIter.next();
                String personSubjectId = statement.getObject().toString();
                String biluSubjectId = statement.getSubject().toString();
                if (!biluResoursceList.contains(biluSubjectId)) biluResoursceList.add(biluSubjectId);
                if (!personRelation.get(personSubjectId).getBiluList().contains(biluSubjectId)) {
                    personRelation.get(personSubjectId).getBiluList().add(biluSubjectId);
                }
            }
            HashSet<String> biluSet = new HashSet<>(biluResoursceList);

            //get all biluId to caseId
            Iterator<Statement> biluCaseIter = jenaLibrary.getStatementsByBatchPO(model, "gongan:gongan.case.bilu", biluSet);
            HashMap<String, String> biluCaseMap = new HashMap<>();
            while (biluCaseIter.hasNext()) {
                Statement statement = biluCaseIter.next();
                biluCaseMap.put(statement.getObject().toString(), statement.getSubject().toString());
            }
            for (String personSubjectId : personRelation.keySet()) {
                Person person = personRelation.get(personSubjectId);
                for (String bilu : person.getBiluList()) {
                    if (biluCaseMap.keySet().contains(bilu)) {
                        if (!person.getCaseList().contains(biluCaseMap.get(bilu)))
                            person.getCaseList().add(biluCaseMap.get(bilu));
                    }
                }
            }
            //enrich person name and identification
            Iterator<Statement> pNamesIter = jenaLibrary.getStatementsByBatchSP(model, personSet, "common:type.object.name");
            while (pNamesIter.hasNext()) {
                Statement statement = pNamesIter.next();
                String personSubjectId = statement.getSubject().toString();
                personRelation.get(personSubjectId).setName(statement.getString());
            }

            Iterator<Statement> pIdentitiesIter = jenaLibrary.getStatementsByBatchSP(model, personSet, "common:person.person.identification");
            while (pIdentitiesIter.hasNext()) {
                Statement statement = pIdentitiesIter.next();
                String personSubjectId = statement.getSubject().toString();
                Resource personIdResource = statement.getResource();
                val personIds = jenaLibrary.getStringValueBySP(model, personIdResource, "common:person.identification.number");
                if (personIds.size() > 0) {
                    personRelation.get(personSubjectId).setIdentity(personIds.get(0));
                }
            }

            personRelationCache = personRelation;
            return personRelationCache;
        } finally {
            jenaLibrary.closeTransaction();
        }
    }

    public Map<String, List<String>> getPhoneCaseRelationCache() {
        if (phoneCaseRelationCache.size() > 0)
            return phoneCaseRelationCache;
        else {
            try {
                jenaLibrary.openReadTransaction();

                //get all thing -> bilu
                Model model = jenaLibrary.getRuntimeModel();
                if (model == null)
                    return new HashMap<>();

                Iterator<Statement> iter = jenaLibrary.getStatementsBySP(model, null, "gongan:gongan.bilu.thing");
                Map<String, List<String>> thingBiluMap = new HashMap<>();
                HashSet<String> biluSet = new HashSet<>();
                while (iter.hasNext()) {
                    val statement = iter.next();
                    if (!thingBiluMap.containsKey(statement.getResource().toString()))
                        thingBiluMap.put(statement.getResource().toString(), new ArrayList<>());

                    thingBiluMap.get(statement.getResource().toString()).add(statement.getSubject().toString());
                    biluSet.add(statement.getSubject().toString());
                }

                //get all biluId to caseId
                Iterator<Statement> biluCaseIter = jenaLibrary.getStatementsByBatchPO(model, "gongan:gongan.case.bilu", biluSet);
                HashMap<String, String> biluCaseMap = new HashMap<>();
                while (biluCaseIter.hasNext()) {
                    Statement statement = biluCaseIter.next();
                    biluCaseMap.put(statement.getObject().toString(), statement.getSubject().toString());
                }

                HashSet<String> phoneSet = new HashSet<>(thingBiluMap.keySet().stream().distinct().collect(Collectors.toList()));
                //enrich phone number
                Iterator<Statement> phoneIter = jenaLibrary.getStatementsByBatchSP(model, phoneSet, "common:thing.phone.phoneNumber");

                while (phoneIter.hasNext()) {
                    Statement statement = phoneIter.next();

                    String phoneNum = statement.getString();
                    if (!phoneNum.isEmpty()) {
                        String phoneSID = statement.getSubject().toString();
                        List<String> biluSIds = thingBiluMap.getOrDefault(phoneSID, null);
                        if (biluSIds != null) {
                            for (String biluSId : biluSIds) {
                                String caseSId = biluCaseMap.getOrDefault(biluSId, null);
                                if (caseSId != null) {
                                    if (!phoneCaseRelationCache.containsKey(phoneNum)) {
                                        phoneCaseRelationCache.put(phoneNum, new ArrayList<>());
                                    }
                                    if (!phoneCaseRelationCache.get(phoneNum).contains(caseSId))
                                        phoneCaseRelationCache.get(phoneNum).add(caseSId);
                                }
                            }
                        }
                    }
                }

                return phoneCaseRelationCache;
            } finally {
                jenaLibrary.closeTransaction();
            }
        }
    }

    public Integer getPersonCount() {
        if (personCountCache != null) return personCountCache;
        try {
            jenaLibrary.openReadTransaction();
            Model model = jenaLibrary.getRuntimeModel();
            if (model == null)
                return 0;

            Iterator<Statement> iter = jenaLibrary.getStatementsByEntityType(model, "common:person.person");
            int count = org.apache.jena.ext.com.google.common.collect.Iterators.size(iter);
            personCountCache = new Integer(count);
            return personCountCache;
        } finally {
            jenaLibrary.closeTransaction();
        }
    }

    public Integer getBiluCount() {
        if (biluCountCache != null) return biluCountCache;
        int count = 0;
        for (String key : allSimpleCases.keySet()) {
            count = count + allSimpleCases.get(key).getBiluNumber();
        }
        biluCountCache = new Integer(count);
        return biluCountCache;
    }

    public Integer getCaseCount() {
        if (caseCountCache != null) return caseCountCache;
        caseCountCache = allSimpleCases.size();
        return caseCountCache;
    }

    public Map<String, Integer> getTagBiluCount() {
        if (tagBiluCount != null) return tagBiluCount;
        try {
            jenaLibrary.openReadTransaction();
            Model model = jenaLibrary.getRuntimeModel();
            if (model == null)
                return new HashMap<>();
            Iterator<Statement> iterator_tag = jenaLibrary.getStatementsBySP(model, null, "common:type.object.tag");
            Map<String, Integer> map = new HashMap();
            iteratorObjectToMap(iterator_tag, map);
            tagBiluCount = map;
            return tagBiluCount;
        } finally {
            jenaLibrary.closeTransaction();
        }
    }

    public Map<String, Integer> getPersonBiluCount() {
        if (personBiluCount != null) return personBiluCount;
        try {
            jenaLibrary.openReadTransaction();
            Model model = jenaLibrary.getRuntimeModel();
            if (model == null)
                return new HashMap<>();

            Iterator<Statement> iterator = jenaLibrary.getStatementsByEntityType(model, "common:person.person");
            HashSet<String> resourceList = new HashSet<>();
            while (iterator.hasNext()) {
                String resource = iterator.next().getSubject().toString();
                resourceList.add(resource);
            }

            Map<String, Integer> map = new HashMap();
            Iterator<Statement> iteratorBiluPerson = jenaLibrary.getStatementsByBatchPO(model, "gongan:gongan.bilu.entity", resourceList);
            iteratorObjectToMap(iteratorBiluPerson, map);

            Map<String, String> idNameMap = new HashMap<>();
            Iterator<Statement> iteratorPersonName = jenaLibrary.getStatementsByBatchSP(model, resourceList, "common:type.object.name");
            while (iteratorPersonName.hasNext()) {
                Statement statement = iteratorPersonName.next();
                idNameMap.put(statement.getSubject().toString(), statement.getObject().toString());
            }

            Map<String, Integer> nameBiluCountMap = new IdentityHashMap<>();
            for (String key : map.keySet()) {
                if (idNameMap.keySet().contains(key)) {
                    nameBiluCountMap.put(new String(idNameMap.get(key)), map.get(key));
                }
            }
            personBiluCount = nameBiluCountMap;

            return personBiluCount;
        } finally {
            jenaLibrary.closeTransaction();
        }
    }

    public void iteratorObjectToMap(Iterator<Statement> iterator, Map<String, Integer> map) {
        while (iterator.hasNext()) {
            Statement statement = iterator.next();
            String object = statement.getObject().toString();
            if (map.keySet().contains(object)) {
                map.put(object, map.get(object) + 1);
            } else {
                map.put(object, 1);
            }
        }
    }

    public Case getCaseById(String subjectId) throws IOException {
        if (caseCache.containsKey(subjectId))
            return caseCache.get(subjectId);
        else {
            try {
                jenaLibrary.openReadTransaction();
                Model model = jenaLibrary.getRuntimeModel();
                if (model == null)
                    throw new NullPointerException("model is null!");

                Case aCase = getCaseInfo(model, model.getResource(subjectId));
                caseCache.put(subjectId, aCase);
                return aCase;
            } finally {
                jenaLibrary.closeTransaction();
            }
        }
    }

    public Bilu getBiluById(String subjectId) throws IOException {
        if (biluCache.containsKey(subjectId))
            return biluCache.get(subjectId);
        else {
            try {
                jenaLibrary.openReadTransaction();
                Model model = jenaLibrary.getRuntimeModel();
                if (model == null)
                    throw new NullPointerException("model is null!");

                Bilu bilu = getBiluInfo(model, model.getResource(subjectId));
                biluCache.put(subjectId, bilu);
                return bilu;
            } finally {
                jenaLibrary.closeTransaction();
            }
        }
    }

    public String getPersonSubjectIdByIdentity(String identity) {
        if (identitySubIdMap != null && identitySubIdMap.containsKey(identity)) {
            return identitySubIdMap.get(identity);
        } else {
            try {
                jenaLibrary.openReadTransaction();
                Model model = jenaLibrary.getRuntimeModel();
                if (model == null) {
                    throw new NullPointerException("model is null!");
                }
                val iterator = jenaLibrary.getStatementsByPOValue(model, "common:person.identification.number", identity);

                if (iterator.hasNext()) {
                    String objectSubId = iterator.next().getSubject().toString();
                    val nextIterator = jenaLibrary.getStatementsByPO(model, "common:person.person.identification", model.getResource(objectSubId));
                    if (nextIterator.hasNext()) {
                        String subId = nextIterator.next().getSubject().toString();
                        identitySubIdMap.put(identity, subId);
                        return subId;
                    } else {
                        LOG.error("can't find id: " + identity);
                        return "";
                    }
                } else {
                    LOG.error("can't find id: " + identity);
                    return "";
                }

            } finally {
                jenaLibrary.closeTransaction();
            }
        }
    }

    public Person getPersonById(String pSubjectId) throws IOException {
        if (personCache.containsKey(pSubjectId))
            return personCache.get(pSubjectId);
        else {
            try {
                jenaLibrary.openReadTransaction();
                Model model = jenaLibrary.getRuntimeModel();
                if (model == null)
                    throw new NullPointerException("model is null!");

                Person person = getPersonInfo(model, model.getResource(pSubjectId));
                personCache.put(pSubjectId, person);
                return person;
            } finally {
                jenaLibrary.closeTransaction();
            }
        }
    }

    private Person getPersonInfo(Model model, Resource resource) throws IOException {
        Person person = personRelationCache.getOrDefault(resource.toString(), null);

        if (person == null) {
            person = new Person();

            person.setSubjectId(resource.toString());
            val pNames = jenaLibrary.getStringValueBySP(model, resource, "common:type.object.name");
            if (pNames.size() > 0)
                person.setName(pNames.get(0));

            val personIdentities = jenaLibrary.getStatementsBySP(model, resource, "common:person.person.identification");
            if (personIdentities.hasNext()) {
                val personIds = jenaLibrary.getStringValueBySP(model, personIdentities.next().getResource(), "common:person.identification.number");
                if (personIds.size() > 0)
                    person.setIdentity(personIds.get(0));
            }

            // set bilus
            val relatedBilus = Lists.newArrayList(jenaLibrary.getStatementsByPO(model, "gongan:gongan.bilu.entity", resource))
                    .stream().map(s -> s.getSubject().toString()).distinct().collect(Collectors.toList());

            person.setBiluList(relatedBilus);

            // set cases
            val relatedCases = Lists.newArrayList(jenaLibrary.getStatementsByBatchPO(model, "gongan:gongan.case.bilu", new HashSet<>(relatedBilus)))
                    .stream().map(s -> s.getSubject().toString()).distinct().collect(Collectors.toList());

            person.setCaseList(relatedCases);
        }

        if (person.getPhone() == null || person.getPhone().isEmpty()) {
            val contactIters = jenaLibrary.getStatementsBySP(model, resource, "common:person.person.contact");
            if (contactIters.hasNext()) {
                val contacts = jenaLibrary.getStringValueBySP(model, contactIters.next().getResource(), "common:person.contact.number");
                if (contacts.size() > 0)
                    person.setPhone(contacts.get(0));
            }
        }

        if (person.getBirthDay() == null || person.getBirthDay().isEmpty()) {
            val birthdays = jenaLibrary.getStringValueBySP(model, resource, "common:person.person.birthDate");
            if (birthdays.size() > 0)
                person.setBirthDay(birthdays.get(0));
        }

        if (person.getGender() == null || person.getGender().isEmpty()) {
            val genders = jenaLibrary.getStringValueBySP(model, resource, "common:person.person.gender");
            if (genders.size() > 0) {
                if (genders.get(0).toLowerCase().equals("female"))
                    person.setGender("女");
                else if (genders.get(0).toLowerCase().equals("male"))
                    person.setGender("男");
            }
        }

        if (person.getIdentity() != null && !person.getIdentity().isEmpty()) {
            String identity = person.getIdentity();
            Request.Builder requestBuilder = new Request.Builder();
            URIBuilder builder = new URIBuilder().setPath(personURI + identity);
            Request request = requestBuilder
                    .header("Authorization", "df620992-d943-4684-924b-b83c9605c47a")
                    .url(builder.toString())
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null && response.body().source() != null) {
                val basicInfoElement = jsonParser.parse(Okio.buffer(response.body().source()).readUtf8()).getAsJsonObject().get("basicInfo");
                if (basicInfoElement != null) {
                    JsonObject basicInfoJO = basicInfoElement.getAsJsonObject();
                    if (basicInfoJO != null) {
                        if (basicInfoJO.has("dateOfBirth")) {
                            val dateOfBirth = basicInfoJO.get("dateOfBirth");
                            if (dateOfBirth != null && !dateOfBirth.getAsString().isEmpty()) {
                                basicInfoJO.remove("dateOfBirth");
                                BasicInfo basicInfo = gson.fromJson(basicInfoJO.toString(), BasicInfo.class);
                                basicInfo.setDateOfBirth(LocalDate.parse(dateOfBirth.getAsString()));
                                person.setBasicInfo(basicInfo);
                            }
                        }
                        HashMap<String, Object> personMap = gson.fromJson(gson.toJson(person), new TypeToken<HashMap<String, Object>>() {
                        }.getType());
                        personMap.putAll(gson.fromJson(basicInfoJO, new TypeToken<HashMap<String, Object>>() {
                        }.getType()));
                        person = gson.fromJson(gson.toJson(personMap), Person.class);
                    }
                }
            }

        }
        return person;
    }

    private Bilu getBiluInfo(Model model, Resource resource) throws IOException {
        Bilu bilu = new Bilu();

        bilu.setSubjectId(resource.toString());

        //bilu id
        List<String> bilu_id_list = jenaLibrary.getStringValueBySP(model, resource, "common:type.object.id");
        bilu.setBiluId(bilu_id_list.size() > 0 ? bilu_id_list.get(0) : "");

        //bilu name
        List<String> bilu_name_list = jenaLibrary.getStringValueBySP(model, resource, "common:type.object.name");
        bilu.setName(bilu_name_list.size() > 0 ? bilu_name_list.get(0) : "");

        //bilu content
        List<String> bilu_content_list = jenaLibrary.getStringValueBySP(model, resource, "common:common.document.contentStream");
        bilu.setContent(bilu_content_list.size() > 0 ? bilu_content_list.get(0) : "");

        // set persons
        val entities = Lists.newArrayList(jenaLibrary.getStatementsBySP(model, resource, "gongan:gongan.bilu.entity")).stream().map(s -> s.getResource().toString()).distinct().collect(Collectors.toList());
        val persons = Lists.newArrayList(jenaLibrary.getStatementsByPOValue(model, "common:type.object.type", "common:person.person")).stream().map(s -> s.getSubject().toString()).distinct().collect(Collectors.toList());
        persons.retainAll(entities);

        List<String> biluConnections = Lists.newArrayList(jenaLibrary.getStatementsByPO(model, "common:common.connection.from", resource)).stream().map(s -> s.getSubject().toString()).distinct().collect(Collectors.toList());
        for (String personSubject : persons) {
            // set person information
            if (!personCache.containsKey(personSubject)) {
                Person person = getPersonInfo(model, model.getResource(personSubject));
                personCache.put(personSubject, person);
            }
            bilu.getPersons().add(personCache.get(personSubject));

            // set connection
            List<String> connectionVal = Lists.newArrayList(jenaLibrary.getStatementsByPO(model, "common:common.connection.to", model.getResource(personSubject))).stream().map(s -> s.getSubject().toString()).distinct().collect(Collectors.toList());
            connectionVal.retainAll(biluConnections);

            String role = "";
            for (String connection : connectionVal) {
                val connectionType = jenaLibrary.getStringValueBySP(model, model.getResource(connection), "common:common.connection.type");

                if (connectionType.contains("common:common.connection.BiluEntityXianyiren"))
                    role += role.contains(Role.BiluEntityXianyiren.toString()) ? "" : (Role.BiluEntityXianyiren.toString() + "；");
                if (connectionType.contains("common:common.connection.BiluEntityZhengren"))
                    role += role.contains(Role.BiluEntityZhengren.toString()) ? "" : (Role.BiluEntityZhengren.toString() + "；");
                if (connectionType.contains("common:common.connection.BiluEntityBaoanren"))
                    role += role.contains(Role.BiluEntityBaoanren.toString()) ? "" : (Role.BiluEntityBaoanren.toString() + "；");
                if (connectionType.contains("common:common.connection.BiluEntityDangshiren"))
                    role += role.contains(Role.BiluEntityDangshiren.toString()) ? "" : (Role.BiluEntityDangshiren.toString() + "；");
                if (connectionType.contains("common:common.connection.BiluEntityShouhairen"))
                    role += role.contains(Role.BiluEntityShouhairen.toString()) ? "" : (Role.BiluEntityShouhairen.toString() + "；");
            }

            int roleLength = role.length();
            if (roleLength > 0) {
                bilu.getConnections().put(personSubject, role.substring(0, roleLength - 1));
            }
        }

        // get all things
        val things = Lists.newArrayList(jenaLibrary.getStatementsBySP(model, resource, "gongan:gongan.bilu.thing")).stream().map(s -> s.getResource().toString()).distinct().collect(Collectors.toList());

        // set phone
        val phones = Lists.newArrayList(jenaLibrary.getStatementsByPOValue(model, "common:type.object.type", "common:thing.phone")).stream().map(s -> s.getSubject().toString()).distinct().collect(Collectors.toList());
        phones.retainAll(things);
        for (String phone : phones) {
            List<String> phoneNums = jenaLibrary.getStringValueBySP(model, model.getResource(phone), "common:thing.phone.phoneNumber");
            if (phoneNums.size() > 0)
                bilu.getPhones().put(phone, phoneNums.get(0));
        }
        // set bank cards
        val bankCards = Lists.newArrayList(jenaLibrary.getStatementsByPOValue(model, "common:type.object.type", "common:thing.bankcard")).stream().map(s -> s.getSubject().toString()).distinct().collect(Collectors.toList());
        bankCards.retainAll(things);
        for (String bankCard : bankCards) {
            List<String> bankCardNums = jenaLibrary.getStringValueBySP(model, model.getResource(bankCard), "common:thing.bankcard.bankCardId");
            if (bankCardNums.size() > 0)
                bilu.getBankCards().put(bankCard, bankCardNums.get(0));
        }

        return bilu;
    }

    private Case getCaseInfo(Model model, Resource resource) throws IOException {
        Case aCase = new Case();

        if (allSimpleCases.containsKey(resource.toString()))
            aCase.SetBy(allSimpleCases.get(resource.toString()));

        else {
            aCase.setSubjectId(resource.toString());

            List<String> csIds = jenaLibrary.getStringValueBySP(model, resource, "common:type.object.id");
            if (csIds.size() > 0)
                aCase.setCaseId(csIds.get(0));

            List<String> csNames = jenaLibrary.getStringValueBySP(model, resource, "common:type.object.name");
            if (csNames.size() > 0)
                aCase.setCaseName(csNames.get(0));

            List<String> csTypes = jenaLibrary.getStringValueBySP(model, resource, "gongan:gongan.case.category");
            if (csTypes.size() > 0)
                aCase.setCaseType(String.join(",", csTypes));

        }

        val biluIter1 = jenaLibrary.getStatementsBySP(model, resource, "gongan:gongan.case.bilu");
        while (biluIter1.hasNext()) {
            Resource biluResource = biluIter1.next().getResource();
            if (!biluCache.containsKey(biluResource)) {
                Bilu bilu = getBiluInfo(model, biluResource);
                biluCache.put(biluResource.toString(), bilu);
            }
            Bilu bilu = biluCache.get(biluResource.toString());
            aCase.getBilus().add(bilu);

            for (String pSubjectId : bilu.getConnections().keySet()) {
                if (!aCase.getConnections().containsKey(pSubjectId))
                    aCase.getConnections().put(pSubjectId, bilu.getConnections().get(pSubjectId));
                else {
                    if (!aCase.getConnections().get(pSubjectId).contains(bilu.getConnections().get(pSubjectId)))
                        aCase.getConnections().put(pSubjectId, bilu.getConnections().get(pSubjectId) + "；" + aCase.getConnections().get(pSubjectId));
                }
            }
        }
        return aCase;
    }

    public List<CaseBaseInfo> getAllCaseBaseInfo() {

        LOG.info("Current Model is " + jenaLibrary.getModelName());

        if (allSimpleCases.size() > 0)
            return allSimpleCases.values().stream().collect(Collectors.toList());
        else {
            try {
                jenaLibrary.openReadTransaction();
                Model model = jenaLibrary.getRuntimeModel();
                if (model == null)
                    return new ArrayList<>();

                val iterator = jenaLibrary.getStatementsByEntityType(model, "gongan:gongan.case");

                val gson = new Gson();
                while (iterator.hasNext()) {
                    CaseBaseInfo aCase = getCaseBaseInfo(model, iterator.next().getSubject());
                    String caseMongoString = mongoCaseRepo.getCase(aCase.getCaseId());
                    CaseMongo caseMongo = gson.fromJson(caseMongoString, CaseMongo.class);
                    if (caseMongo != null && caseMongo.getAJBH() != null && !caseMongo.getAJBH().isEmpty()) {
                        caseMongoMap.put(aCase.getCaseId(), caseMongo);
                        if (aCase.getCaseType() == null || aCase.getCaseType().isEmpty())
                            aCase.setCaseType(caseMongo.getAJLXName());
                        if (caseMongo.getAJMC() != null && !caseMongo.getAJMC().isEmpty())
                            aCase.setCaseName(caseMongo.getAJMC());
                    }

                    allSimpleCases.put(aCase.getSubjectId(), aCase);
                }

                return allSimpleCases.values().stream().collect(Collectors.toList());

            } finally {
                jenaLibrary.closeTransaction();
            }
        }
    }

    public CaseBaseInfo getCaseBaseInfoById(String subjectId) {
        if (allSimpleCases.size() > 0)
            return allSimpleCases.getOrDefault(subjectId, new CaseBaseInfo());
        else {
            try {
                jenaLibrary.openReadTransaction();
                Model model = jenaLibrary.getRuntimeModel();
                if (model == null)
                    throw new NullPointerException("model is null!");

                val iterator = jenaLibrary.getStatementsByEntityType(model, "gongan:gongan.case");

                while (iterator.hasNext()) {
                    CaseBaseInfo aCase = getCaseBaseInfo(model, iterator.next().getSubject());
                    allSimpleCases.put(aCase.getSubjectId(), aCase);
                }

                return allSimpleCases.getOrDefault(subjectId, new CaseBaseInfo());

            } finally {
                jenaLibrary.closeTransaction();
            }
        }
    }

    private CaseBaseInfo getCaseBaseInfo(Model model, Resource resource) {
        CaseBaseInfo caseBaseInfo = new CaseBaseInfo();

        caseBaseInfo.setSubjectId(resource.toString());

        List<String> ids = jenaLibrary.getStringValueBySP(model, resource, "common:type.object.id");
        if (ids.size() > 0)
            caseBaseInfo.setCaseId(ids.get(0));

        List<String> names = jenaLibrary.getStringValueBySP(model, resource, "common:type.object.name");
        if (names.size() > 0)
            caseBaseInfo.setCaseName(names.get(0));

        List<String> types = jenaLibrary.getStringValueBySP(model, resource, "gongan:gongan.case.category");
        if (types.size() > 0)
            caseBaseInfo.setCaseType(String.join(",", types));

        // count of bilu
        val biluIter1 = jenaLibrary.getStatementsBySP(model, resource, "gongan:gongan.case.bilu");
        if (biluIter1.hasNext())
            caseBaseInfo.setBiluNumber(Iterators.size(biluIter1));

        // set suspect
        caseBaseInfo.setSuspects(new LinkedList<>());

        val bilus = Lists.newArrayList(jenaLibrary.getStatementsBySP(model, resource, "gongan:gongan.case.bilu"))
                .stream().map(s -> s.getResource().toString()).distinct().collect(Collectors.toList());

        val biluSet = new HashSet<String>(bilus);
        List<String> biluConnections = Lists.newArrayList(jenaLibrary.getStatementsByBatchPO(model, "common:common.connection.from", biluSet))
                .stream().map(s -> s.getSubject().toString()).distinct().collect(Collectors.toList());

        for (String connection : biluConnections) {
            List<String> connectTypes = jenaLibrary.getStringValueBySP(model, model.getResource(connection), "common:common.connection.type");
            if (connectTypes.contains("common:common.connection.BiluEntityXianyiren")) {
                val toStatements = Lists.newArrayList(jenaLibrary.getStatementsBySP(model, model.getResource(connection), "common:common.connection.to"))
                        .stream().map(s -> s.getResource().toString()).distinct().collect(Collectors.toList());

                val toSet = new HashSet<String>(toStatements);
                caseBaseInfo.getSuspects().addAll(jenaLibrary.getStringValuesByBatchSP(model, toSet, "common:type.object.name"));
            }
        }

        caseBaseInfo.setSuspects(caseBaseInfo.getSuspects().stream().distinct().collect(Collectors.toList()));

        return caseBaseInfo;
    }

    public String getSubjectIdById(String id) {
        try {
            jenaLibrary.openReadTransaction();
            Model model = jenaLibrary.getRuntimeModel();
            if (model == null)
                throw new NullPointerException("model is null!");

            val iterator = jenaLibrary.getStatementsById(model, id);

            if (iterator.hasNext()) {
                return iterator.next().getSubject().toString();
            } else {
                LOG.error("can't find id: " + id);
                return "";
            }
        } finally {
            jenaLibrary.closeTransaction();
        }
    }

    public String getBiluCrimeComponent(String biluSubjectId) {
        try {
            jenaLibrary.openReadTransaction();
            Model model = jenaLibrary.getRuntimeModel();
            if (model == null)
                throw new NullPointerException("model is null!");

            val components = jenaLibrary.getStringValueBySP(model, model.getResource(biluSubjectId), "gongan:gongan.bilu.crimeComponentString");
            if (components.size() > 0)
                return components.get(0);
            else
                return "";

        } finally {
            jenaLibrary.closeTransaction();
        }

    }
}
