package com.mjurinic.bzsw.helpers;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.List;

public class OntologyHelper {

    private static OntologyHelper instance;

    // An OWLOntologyManager manages a set of ontologies. It is the main point for creating, loading and accessing ontologies.
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    // An interface for creating entities, class expressions and axioms.
    private OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();

    private OntologyHelper() {}

    public OWLAxiomChange addDisjointClass(OWLOntology ontology, OWLClass a, OWLClass b) {
        return new AddAxiom(ontology, dataFactory.getOWLDisjointClassesAxiom(a, b));
    }

    public OWLAxiomChange addDataProperty(OWLOntology ontology, OWLIndividual individual,
                                            OWLDataProperty property, int value) {

        return new AddAxiom(
                ontology,
                dataFactory.getOWLDataPropertyAssertionAxiom(property, individual, value)
        );
    }

    public OWLAxiomChange addObjectProperty(OWLOntology ontology, OWLIndividual parentIndividual,
                                            OWLObjectProperty property, OWLIndividual childIndividual) {

        return new AddAxiom(
            ontology,
            dataFactory.getOWLObjectPropertyAssertionAxiom(property, parentIndividual, childIndividual)
        );
    }

    public OWLAxiomChange createDataProperty(OWLOntology ontology, String iri) {
        OWLDataProperty dataProperty = dataFactory.getOWLDataProperty(convertStringToIRI(iri));

        return new AddAxiom(
            ontology,
            dataFactory.getOWLFunctionalDataPropertyAxiom(dataProperty)
        );
    }

    public OWLAxiomChange createObjectProperty(OWLOntology ontology, String iri, OWLClass parentRef, OWLClass childRef) {
        OWLObjectProperty objectProperty = dataFactory.getOWLObjectProperty(convertStringToIRI(iri));

        return new AddAxiom(
            ontology,
            dataFactory.getOWLSubClassOfAxiom(parentRef, dataFactory.getOWLObjectSomeValuesFrom(objectProperty, childRef))
        );
    }

    public OWLAxiomChange createIndividual(OWLOntology ontology, OWLClass belongsClass, String iri) {
        return new AddAxiom(
                ontology,
                dataFactory.getOWLClassAssertionAxiom(belongsClass, dataFactory.getOWLNamedIndividual(convertStringToIRI(iri)))
        );
    }

    public void applyChanges(List<OWLAxiomChange> changes) {
        manager.applyChanges(changes);
    }

    public void applyChange(OWLAxiomChange change) {
        manager.applyChange(change);
    }

    public OWLAxiomChange createSubClass(OWLOntology ontology, OWLClass subClass, OWLClass superClass) {
        return new AddAxiom(ontology, dataFactory.getOWLSubClassOfAxiom(subClass, superClass));
    }

    public OWLClass createClass(String iri) {
        return dataFactory.getOWLClass(convertStringToIRI(iri));
    }

    public OWLOntology readOntology(OWLOntologyDocumentSource source) {
        try {
            return manager.loadOntologyFromOntologyDocument(source);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void saveOntology(OWLOntology ontology, String fileName) {
        try {
            manager.saveOntology(ontology, IRI.create(new File(fileName)));

        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        }
    }

    public OWLOntology createOntology(String iri) {
        try {
            return manager.createOntology(convertStringToIRI(iri));

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        return null;
    }

    public IRI convertStringToIRI(String iri) {
        return IRI.create(iri);
    }

    public OWLIndividual getIndividual(String iri) {
        return dataFactory.getOWLNamedIndividual(convertStringToIRI(iri));
    }

    public OWLDataProperty getDataProperty(String iri) {
        return dataFactory.getOWLDataProperty(convertStringToIRI(iri));
    }

    public OWLObjectProperty getObjectProperty(String iri) {
        return dataFactory.getOWLObjectProperty(convertStringToIRI(iri));
    }

    public static OntologyHelper getInstance() {
        if (instance == null) {
            instance = new OntologyHelper();
        }

        return instance;
    }
}
