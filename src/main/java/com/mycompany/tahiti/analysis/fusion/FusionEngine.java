package com.mycompany.tahiti.analysis.fusion;

import com.mycompany.tahiti.analysis.jena.JenaLibrary;
import lombok.Data;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.ResourceUtils;
import org.apache.log4j.Logger;

import java.util.*;

public class FusionEngine {
    JenaLibrary jenaLibrary;

    private static final Logger LOG = Logger.getLogger(FusionEngine.class);

    public FusionEngine(JenaLibrary jenaLibrary, String subjectPrefix) {
        this.jenaLibrary = jenaLibrary;
        this.prefix = subjectPrefix;
    }

    String prefix;

    public Model generateFusionModel(){
        try {
            LOG.info("start conflation ... ");
            jenaLibrary.openReadTransaction();
            Model model = jenaLibrary.getRuntimeModel();

            Map<String, String> idMap = personConflation(model);

            LOG.info("idMap is finished! size is " + idMap.size());
            // cope a new model
            Model newModel = jenaLibrary.deepCopyModel(jenaLibrary.getRuntimeModel());

            // set data into new Model
            for(String from : idMap.keySet()) {
                ResourceUtils.renameResource(newModel.getResource(from), idMap.get(from));
            }

            LOG.info("conflation is generated, newModel is generated!");
            return newModel;
        } finally {
            jenaLibrary.closeTransaction();
        }
    }

    private Map<String, String> personConflation(Model model) {
        Map<String, PersonFeatures> persons = new HashMap<>();
        getAllPersonForFusion(model, persons);

        val identitiesBucket = generateBuckets(BucketType.Identity, persons);
        val phoneBucket = generateBuckets(BucketType.Phone, persons);
        val nameCaseBucket = generateBuckets(BucketType.NameCase, persons);

        Map<String, String> subjectConflation = new HashMap<>();
        processBucket(identitiesBucket, subjectConflation);
        processBucket(phoneBucket, subjectConflation);
        processBucket(nameCaseBucket, subjectConflation);

        return subjectConflation;
    }

    private void getAllPersonForFusion(Model model, Map<String, PersonFeatures> persons){
        Iterator<Statement> iterator = jenaLibrary.getStatementsByEntityType(model,"common:person.person");

        HashSet<String> personSet = new HashSet<>();
        //get all person subjectId
        while (iterator.hasNext()) {
            personSet.add(iterator.next().getSubject().toString());
        }

        //get all person subjectId to biluId
        Map<String, List<String>> personBiluMap = jenaLibrary.getOSMapByBatchPO(model, "gongan:gongan.bilu.entity", personSet);

        List<String> biluList = new ArrayList<>();
        for(List<String> bilus : personBiluMap.values()) biluList.addAll(bilus);
        HashSet<String> biluSet = new HashSet<>(biluList);

        //get all biluId to caseId
        Map<String, List<String>> biluCaseMap = jenaLibrary.getOSMapByBatchPO(model, "gongan:gongan.case.bilu", biluSet);

        // get all person names
        Map<String, List<String>> personNameMap = jenaLibrary.getSOMapByBatchSP(model, personSet, "common:type.object.name");

        // get all person to identification
        Map<String, List<String>> personIdentityMap = jenaLibrary.getSOMapByBatchSP(model, personSet, "common:person.person.identification");

        List<String> identityList = new ArrayList<>();
        for(List<String> ids : personIdentityMap.values()) identityList.addAll(ids);
        HashSet<String> idenities = new HashSet<>(identityList);

        // get identity to number
        Map<String, List<String>> identityNumberMap = jenaLibrary.getSOMapByBatchSP(model, idenities, "common:person.identification.number");

        // get person to contact
        Map<String, List<String>> personContactMap = jenaLibrary.getSOMapByBatchSP(model, personSet, "common:person.person.contact");

        List<String> contactList = new ArrayList<>();
        for(List<String> cons : personContactMap.values()) contactList.addAll(cons);
        HashSet<String> contacts = new HashSet<>(contactList);

        // get identity to number
        Map<String, List<String>> contactNumberMap = jenaLibrary.getSOMapByBatchSP(model, contacts, "common:person.contact.number");

        for (String personSubjectId : personSet){
            PersonFeatures person = new PersonFeatures();
            person.setSubjectId(personSubjectId);
            person.setNames(new HashSet<>(personNameMap.getOrDefault(personSubjectId, new LinkedList<>())));

            val personContacts = personContactMap.getOrDefault(personSubjectId, new LinkedList<>());
            for(String contact : personContacts){
                person.getPhones().addAll(contactNumberMap.getOrDefault(contact, new LinkedList<>()));
            }

            val bilus = personBiluMap.getOrDefault(personSubjectId, new LinkedList<>());
            for(String bilu : bilus) {
                person.getCaseList().addAll(biluCaseMap.getOrDefault(bilu, new LinkedList<>()));
            }

            val personIdentities = personIdentityMap.getOrDefault(personSubjectId, new LinkedList<>());
            for(String identity : personIdentities) {
                val numbers = identityNumberMap.getOrDefault(identity, new LinkedList<>());
                for(String number : numbers)
                {
                    if(number != null & !number.isEmpty())
                        person.setIdentity(number);
                }
            }

            persons.put(personSubjectId, person);
        }
    }

    private Map<String, List<PersonFeatures>> generateBuckets(BucketType type, Map<String, PersonFeatures> persons){
        switch (type) {
            case Identity:
                Map<String, List<PersonFeatures>> identitiesBucket = new HashMap<>();
                for(String sId : persons.keySet()){
                    PersonFeatures person = persons.get(sId);
                    if(person.getIdentity() != null && !person.getIdentity().isEmpty()){
                        if(!identitiesBucket.containsKey(person.getIdentity())){
                            identitiesBucket.put(person.getIdentity(), new ArrayList<>());
                        }
                        identitiesBucket.get(person.getIdentity()).add(person);
                    }
                }

                return identitiesBucket;

            case Phone:
                Map<String, List<PersonFeatures>> phoneBucket = new HashMap<>();
                for(String sId : persons.keySet()) {
                    PersonFeatures person = persons.get(sId);
                    for (String phone : person.getPhones()) {
                        if(phone == null || phone.isEmpty())
                            continue;
                        if (!phoneBucket.containsKey(phone)) {
                            phoneBucket.put(phone, new ArrayList<>());
                        }
                        phoneBucket.get(phone).add(person);
                    }
                }
                return phoneBucket;

            case NameCase:
                Map<String, List<PersonFeatures>> nameCaseBucket = new HashMap<>();
                for(String sId : persons.keySet()) {
                    PersonFeatures person = persons.get(sId);
                    for (String name : person.getNames()) {
                        if(name == null || name.isEmpty())
                            continue;

                        for(String caseSubject : person.getCaseList()) {
                            String bucketId = name + caseSubject;
                            if (!nameCaseBucket.containsKey(bucketId)) {
                                nameCaseBucket.put(bucketId, new ArrayList<>());
                            }
                            nameCaseBucket.get(bucketId).add(person);
                        }
                    }
                }
                return nameCaseBucket;

            default:
                throw new RuntimeException("Now just allow type: Identity, Phone, NameCase! not allow " + type);
        }
    }

    private void processBucket(Map<String, List<PersonFeatures>> bucket, Map<String, String> subjectConflation){
        for(String bucketId : bucket.keySet()){
            val bucketValue = bucket.get(bucketId);
            if(bucketValue.size() > 1){
                String id = getTargetSubject(bucketValue, subjectConflation);
                for(PersonFeatures p : bucketValue){
                    subjectConflation.put(p.subjectId, id);
                }
            }
        }

    }

    private String getTargetSubject(List<PersonFeatures> personsInBucket, Map<String, String> subjectConflation){
        for(PersonFeatures person : personsInBucket){
            if(subjectConflation.keySet().contains(person.getSubjectId()))
                return subjectConflation.get(person.getSubjectId());
        }

        return prefix + UUID.randomUUID().toString();
    }

    @Data
    private class PersonFeatures {
        private String subjectId;
        private Set<String> names = new HashSet<>();
        private Set<String> phones = new HashSet<>();
        private String identity;
        // case SubjectId
        private Set<String> caseList = new HashSet<>();
    }

    private enum BucketType {
        Identity,
        Phone,
        NameCase;
    }
}
