package com.mycompany.tahiti.analysis.fusion;

import com.google.common.collect.Lists;
import com.mycompany.tahiti.analysis.jena.JenaLibrary;
import lombok.Data;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class FusionEngine {
    @Autowired
    JenaLibrary jenaLibrary;

    private static final String prefix  = "http://knowledge.richinfo.com/";

    public Model GenerateFusionModel(String newModelName){
        try {
            jenaLibrary.openReadTransaction();
            Model model = jenaLibrary.getRuntimeModel();

            Map<String, String> idMap = PersonConflation(model);

            // 产生新的model
            Model newModel = jenaLibrary.getModel(newModelName);

            // set data into new Model
            // todo

            return newModel;
        } finally {
            jenaLibrary.closeTransaction();
        }
    }

    private Map<String, String> PersonConflation(Model model) {
        Map<String, PersonForFusion> persons = new HashMap<>();
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

    private void getAllPersonForFusion(Model model, Map<String, PersonForFusion> persons){
        Iterator<Statement> iterator = jenaLibrary.getStatementsByEntityType(model,"common:person.person");
        while (iterator.hasNext()){
            Resource personSubject = iterator.next().getSubject();

            PersonForFusion person = new PersonForFusion();

            person.setSubjectId(personSubject.toString());
            val pNames = jenaLibrary.getStringValueBySP(model, personSubject, "common:type.object.name");
            person.getNames().addAll(pNames);

            val personIdentities = jenaLibrary.getStatementsBySP(model, personSubject, "common:person.person.identification");
            if (personIdentities.hasNext()) {
                val personIds = jenaLibrary.getStringValueBySP(model, personIdentities.next().getResource(), "common:person.identification.number");
                for(String personId : personIds){
                    if(personId != null && !personId.isEmpty()) {
                        person.setIdentity(personId);
                        break;
                    }
                }
            }

            val contactIters = jenaLibrary.getStatementsBySP(model, personSubject, "common:person.person.contact");
            if (contactIters.hasNext()) {
                val contacts = jenaLibrary.getStringValueBySP(model, contactIters.next().getResource(), "common:person.contact.number");
                person.getPhones().addAll(contacts);
            }

            val relatedBilus = Lists.newArrayList(jenaLibrary.getStatementsByPO(model, "gongan:gongan.bilu.entity", personSubject))
                    .stream().map(s -> s.getSubject().toString()).distinct().collect(Collectors.toList());

            // set cases
            val relatedCases = Lists.newArrayList(jenaLibrary.getStatementsByBatchPO(model, "gongan:gongan.case.bilu", relatedBilus))
                    .stream().map(s -> s.getSubject().toString()).distinct().collect(Collectors.toList());
            person.getCaseList().addAll(relatedCases);

            persons.put(person.getSubjectId(), person);
        }
    }

    private Map<String, List<PersonForFusion>> generateBuckets(BucketType type, Map<String, PersonForFusion> persons){
        switch (type) {
            case Identity:
                Map<String, List<PersonForFusion>> identitiesBucket = new HashMap<>();
                for(String sId : persons.keySet()){
                    PersonForFusion person = persons.get(sId);
                    if(person.getIdentity() != null && !person.getIdentity().isEmpty()){
                        if(!identitiesBucket.containsKey(person.getIdentity())){
                            identitiesBucket.put(person.getIdentity(), new ArrayList<>());
                        }
                        identitiesBucket.get(person.getIdentity()).add(person);
                    }
                }

                return identitiesBucket;

            case Phone:
                Map<String, List<PersonForFusion>> phoneBucket = new HashMap<>();
                for(String sId : persons.keySet()) {
                    PersonForFusion person = persons.get(sId);
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
                Map<String, List<PersonForFusion>> nameCaseBucket = new HashMap<>();
                for(String sId : persons.keySet()) {
                    PersonForFusion person = persons.get(sId);
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
                return null;
        }
    }

    private void processBucket(Map<String, List<PersonForFusion>> bucket, Map<String, String> subjectConflation){
        for(String bucketId : bucket.keySet()){

            val bucketValue = bucket.get(bucketId);
            if(bucketValue.size() > 0){
                String id = getKeptSubject(bucketValue, subjectConflation);
                for(PersonForFusion p : bucketValue){
                    subjectConflation.put(p.subjectId, id);
                }
            }
        }

    }

    private String getKeptSubject(List<PersonForFusion> personsInBucket, Map<String, String> subjectConflation){

        return prefix + UUID.randomUUID().toString();
    }

    @Data
    private class PersonForFusion{
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
