
--
-- Data for Name: vocabularies; Type: TABLE DATA; Schema: public; Owner: marketplace
--

INSERT INTO public.vocabularies (code, accessible_at, description, label) VALUES ('publication-type', NULL, 'The Bibliographic Ontology describes bibliographic things on the semantic Web in RDF. This ontology can be used as a citation ontology, as a document classification ontology, or simply as a way to describe any kind of document in RDF. It has been inspired by many existing document description metadata formats, and can be used as a common ground for converting other bibliographic data sources.', 'The Bibliographic Ontology Concept Scheme');


--
-- Data for Name: concepts; Type: TABLE DATA; Schema: public; Owner: marketplace
--

INSERT INTO public.concepts (code, vocabulary_code, definition, label, notation, ord, uri) VALUES ('Journal', 'publication-type', 'Journal', 'Journal', 'Journal', 1, 'http://purl.org/ontology/bibo/Journal');
INSERT INTO public.concepts (code, vocabulary_code, definition, label, notation, ord, uri) VALUES ('Book', 'publication-type', 'Book', 'Book', 'Book', 2, 'http://purl.org/ontology/bibo/Book');
INSERT INTO public.concepts (code, vocabulary_code, definition, label, notation, ord, uri) VALUES ('Conference', 'publication-type', 'Conference', 'Conference', 'Conference', 3, 'http://purl.org/ontology/bibo/Conference');
INSERT INTO public.concepts (code, vocabulary_code, definition, label, notation, ord, uri) VALUES ('Article', 'publication-type', 'Article', 'Article', 'Article', 4, 'http://purl.org/ontology/bibo/Article');
INSERT INTO public.concepts (code, vocabulary_code, definition, label, notation, ord, uri) VALUES ('Pre-Print', 'publication-type', 'Pre Print', 'Pre-Print', 'Pre-Print', 5, 'http://purl.org/ontology/bibo/Pre-Print');


--
-- Data for Name: property_types; Type: TABLE DATA; Schema: public; Owner: marketplace
--

INSERT INTO public.property_types (code, label, ord, type) VALUES ('journal', 'Journal', 18, 'STRING');
INSERT INTO public.property_types (code, label, ord, type) VALUES ('conference', 'Conference', 19, 'STRING');
INSERT INTO public.property_types (code, label, ord, type) VALUES ('volume', 'Volume', 20, 'STRING');
INSERT INTO public.property_types (code, label, ord, type) VALUES ('issue', 'Issue', 21, 'STRING');
INSERT INTO public.property_types (code, label, ord, type) VALUES ('pages', 'Pages', 22, 'STRING');
INSERT INTO public.property_types (code, label, ord, type) VALUES ('year', 'Year', 23, 'INT');
INSERT INTO public.property_types (code, label, ord, type) VALUES ('timestamp', 'Timestamp', 24, 'DATE');
INSERT INTO public.property_types (code, label, ord, type) VALUES ('publication-type', 'Publication type', 25, 'CONCEPT');
INSERT INTO public.property_types (code, label, ord, type) VALUES ('doi', 'DOI', 26, 'STRING');


--
-- Data for Name: property_types_vocabularies; Type: TABLE DATA; Schema: public; Owner: marketplace
--

INSERT INTO public.property_types_vocabularies (property_type_code, vocabulary_code) VALUES ('publication-type', 'publication-type');

