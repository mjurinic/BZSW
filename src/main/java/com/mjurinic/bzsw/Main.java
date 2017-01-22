package com.mjurinic.bzsw;

import com.mjurinic.bzsw.helpers.OntologyHelper;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import uk.ac.manchester.cs.jfact.JFactFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main {

    private static final String ONTOLOGY_BASE_IRI = "http://mjurinic.com/bzsw/ontologies/example.owl";

    private static OntologyHelper ontologyHelper = OntologyHelper.getInstance();

    public static void main(String[] args) {
        // Create and Save new ontology
        OWLOntology ontology = ontologyHelper.createOntology(ONTOLOGY_BASE_IRI);
        ontologyHelper.saveOntology(ontology, "example.owl");

        // Define classes
        OWLClass animalClass = ontologyHelper.createClass(ONTOLOGY_BASE_IRI + "#Animal");
        OWLClass wingedAnimalClass = ontologyHelper.createClass(ONTOLOGY_BASE_IRI + "#WingedAnimal");
        OWLClass mammalAnimalClass = ontologyHelper.createClass(ONTOLOGY_BASE_IRI + "#MammalAnimal");
        OWLClass batClass = ontologyHelper.createClass(ONTOLOGY_BASE_IRI + "#Bat");
        OWLClass mosquitoClass = ontologyHelper.createClass(ONTOLOGY_BASE_IRI + "#Mosquito");
        OWLClass waspClass = ontologyHelper.createClass(ONTOLOGY_BASE_IRI + "#Wasp");
        OWLClass bearClass = ontologyHelper.createClass(ONTOLOGY_BASE_IRI + "#Bear");

        // Create relationships
        List<OWLAxiomChange> patches = new ArrayList<>();
        patches.add(ontologyHelper.createSubClass(ontology, wingedAnimalClass, animalClass));
        patches.add(ontologyHelper.createSubClass(ontology, mammalAnimalClass, animalClass));
        patches.add(ontologyHelper.createSubClass(ontology, batClass, wingedAnimalClass));
        patches.add(ontologyHelper.createSubClass(ontology, batClass, mammalAnimalClass));
        patches.add(ontologyHelper.createSubClass(ontology, mosquitoClass, wingedAnimalClass));
        patches.add(ontologyHelper.createSubClass(ontology, waspClass, wingedAnimalClass));
        patches.add(ontologyHelper.createSubClass(ontology, bearClass, mammalAnimalClass));

        // Disjoint wasps and mosquito's (this is not a reflexive relation so we have to do it in both directions)
        patches.add(ontologyHelper.addDisjointClass(ontology, waspClass, mosquitoClass));
        patches.add(ontologyHelper.addDisjointClass(ontology, mosquitoClass, waspClass));

        // Patch and save ontology
        updateOntology(ontology, patches);

        // Create individuals
        patches.clear();
        patches.add(ontologyHelper.createIndividual(ontology, bearClass, ONTOLOGY_BASE_IRI + "#PolarBear"));
        patches.add(ontologyHelper.createIndividual(ontology, mosquitoClass, ONTOLOGY_BASE_IRI + "#TigerMosquito"));
        patches.add(ontologyHelper.createIndividual(ontology, waspClass, ONTOLOGY_BASE_IRI + "#EuropeanHornetWasp"));
        patches.add(ontologyHelper.createIndividual(ontology, batClass, ONTOLOGY_BASE_IRI + "#Megabat"));

        updateOntology(ontology, patches);

        // Create object properties
        patches.clear();
        patches.add(ontologyHelper.createObjectProperty(ontology, ONTOLOGY_BASE_IRI + "#isEating", batClass, mosquitoClass));
        patches.add(ontologyHelper.createObjectProperty(ontology, ONTOLOGY_BASE_IRI + "#isFood", mosquitoClass, batClass));

        updateOntology(ontology, patches);

        // Add object properties
        patches.clear();
        patches.add(
                 ontologyHelper.addObjectProperty(
                         ontology,
                         ontologyHelper.getIndividual(ONTOLOGY_BASE_IRI + "#Megabat"),
                         ontologyHelper.getObjectProperty(ONTOLOGY_BASE_IRI + "#isEating"),
                         ontologyHelper.getIndividual(ONTOLOGY_BASE_IRI + "#TigerMosquito"))
        );

        patches.add(
                ontologyHelper.addObjectProperty(
                        ontology,
                        ontologyHelper.getIndividual(ONTOLOGY_BASE_IRI + "#TigerMosquito"),
                        ontologyHelper.getObjectProperty(ONTOLOGY_BASE_IRI + "#isFood"),
                        ontologyHelper.getIndividual(ONTOLOGY_BASE_IRI + "#Megabat"))
        );

        updateOntology(ontology, patches);

        // Create data properties
        ontologyHelper.applyChange(ontologyHelper.createDataProperty(ontology, ONTOLOGY_BASE_IRI + "#hasWings"));

        // Add data properties
        patches.clear();

        patches.add(
                ontologyHelper.addDataProperty(
                        ontology,
                        ontologyHelper.getIndividual(ONTOLOGY_BASE_IRI + "#TigerMosquito"),
                        ontologyHelper.getDataProperty(ONTOLOGY_BASE_IRI + "#hasWings"),
                        2)
        );

        patches.add(
                ontologyHelper.addDataProperty(
                        ontology,
                        ontologyHelper.getIndividual(ONTOLOGY_BASE_IRI + "#EuropeanHornetWasp"),
                        ontologyHelper.getDataProperty(ONTOLOGY_BASE_IRI + "#hasWings"),
                        4)
        );

        updateOntology(ontology, patches);


        // Reasoner setup
        OWLReasonerFactory reasonerFactory = new JFactFactory();

        OWLReasonerConfiguration config = new SimpleConfiguration(50000);
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, config);

        // Ask the reasoner to classify the ontology
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

        // get a list of unsatisfiable classes
        Node<OWLClass> bottomNode = reasoner.getUnsatisfiableClasses();

        // leave owl:Nothing out
        Set<OWLClass> unsatisfiable = bottomNode.getEntitiesMinusBottom();

        if (!unsatisfiable.isEmpty()) {
            System.out.println("The following classes are unsatisfiable: ");

            for (OWLClass cls : unsatisfiable) {
                System.out.println(cls.getIRI().getFragment());
            }
        } else {
            System.out.println("There are no unsatisfiable classes");
        }

        // Look up and print all direct subclasses for all classes
        for (OWLClass c : ontology.getClassesInSignature()) {
            NodeSet<OWLClass> subClasses = reasoner.getSubClasses(c, true);

            for (OWLClass subClass : subClasses.getFlattened()) {
                System.out.println(subClass.getIRI().getFragment() + "\tsubclass of\t"
                        + c.getIRI().getFragment());
            }
        }

        // for each class, look up the instances
        for (OWLClass c : ontology.getClassesInSignature()) {
            NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(c, true);

            for (OWLNamedIndividual i : instances.getFlattened()) {
                System.out.println(i.getIRI().getFragment() + "\tinstance of\t" + c.getIRI().getFragment());

                // look up all property assertions
                for (OWLObjectProperty op : ontology.getObjectPropertiesInSignature()) {
                    NodeSet<OWLNamedIndividual> petValuesNodeSet = reasoner.getObjectPropertyValues(i, op);
                    for (OWLNamedIndividual value : petValuesNodeSet.getFlattened()) {
                        System.out.println(i.getIRI().getFragment() + "\t"
                                + op.getIRI().getFragment() + "\t"
                                + value.getIRI().getFragment());
                    }
                }
            }
        }
    }

    /**
     * Patch and save ontology
     * @param ontology
     * @param patches
     */
    private static void updateOntology(OWLOntology ontology, List<OWLAxiomChange> patches) {
        ontologyHelper.applyChanges(patches);
        ontologyHelper.saveOntology(ontology, "example.owl");
    }
}
