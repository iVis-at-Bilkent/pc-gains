package PC_Gains.Procedures;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.Sbgn;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.sbgn.bindings.Arc.End;
import org.sbgn.bindings.Arc.Start;
import org.sbgn.bindings.Glyph.State;
import org.sbgn.bindings.Label;
public class App {

	public class GOINodeObject {

		GOINodeObject(String id, double depth) {
			this.nodeId = id;
			this.dist = depth;
		}

		String nodeId;
		double dist = 999;
	}

	public class GOINodeContainer {
		HashMap<String, GOINodeObject> GOINodeObjectList = new HashMap<String, GOINodeObject>();

		HashMap<String, Node> nodesList = new HashMap<String, Node>();
		HashMap<String, Relationship> relsList = new HashMap<String, Relationship>();
		HashMap<String, List<Path>> listOfpathslist = new HashMap<String, List<Path>>();
		Set<Path> pathList = new HashSet<Path>();
		Set<Path> pathListforlabelallglyphs = new HashSet<Path>();
		Set<String> finalIds = new HashSet<String>();
		String rootId;
		Path apath;
	}

	class PathsBetweenRoot {	
		
		PathsBetweenRoot( String id , double depth){
			this.nodeId= id;
			this.dist = depth;		
			
		}
		String nodeId;
		double dist = 999;	
		Set<Object> FreshIdListToIterate = new HashSet<Object>();
		Set<Object> OldIdListToIterate = new HashSet<Object>();
		Set<Path> pathList = new HashSet<Path>();	
	    Set<PathListITem> PathListContainer = new HashSet<PathListITem>();	;	
		HashMap<String, FreshIdItem> FreshIdMap = new HashMap<String, FreshIdItem>();

	}

	class FreshIdItem{
		
		FreshIdItem(Object sid , Object pid){
			this.pid = pid;
			this.sid = sid;	
			
		}
		Object pid;
		 Object sid;
	}

	class PathListITem{
		Set<String> pathNodeIdList= new HashSet<String>();
		
		PathListITem(String key, String endNodeId, double d){
			this.key =key;
			this.endNodeDepth = d;
			this.endNodeId = endNodeId;		
		}
		String endNodeId;
		String key;
		double endNodeDepth;
		
	}

	public class Output {

		Output(String out) {
			this.out = out;
		}

		public String out;
	}

	public class StreamObject {
		HashMap<String, Node> nodesList = new HashMap<String, Node>();
		HashMap<String, Relationship> relsList = new HashMap<String, Relationship>();
		Set<Path> pathList = new HashSet<Path>();
		Path apath;
	}

	@Context
	public GraphDatabaseService db;

	@Procedure(name = "InsertGraph", mode = Mode.WRITE)
	public Stream<Output> InsertGraph(@Name("sbgnML") String sbgnML) throws JAXBException {
		if (sbgnML == null) {
			return null;
		}

		sbgnML = sbgnML.replace("libsbgn/0.3", "libsbgn/0.2");
		Sbgn sbgn = readFromString(sbgnML);
		// map is a container for the glyphs and arcs
		org.sbgn.bindings.Map map = sbgn.getMap();

		for (Glyph g : ((org.sbgn.bindings.Map) map).getGlyph()) {
			InsertGlyph(g);

		}

		for (Arc a : ((org.sbgn.bindings.Map) map).getArc()) {
			InsertArc(a);

		}
		/*
		String compartmnetResideInQuery ="Match (a) where a.compRef <> {notexist} with a match (b:compartment) where b.id = a.compRef with a, b merge (a)-[:resideIn {aid: {aid}}]->(b)";
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("notexist", "notexist");
		parameters.put("aid", 999);

		db.execute(compartmnetResideInQuery, parameters);
		*/
		return Stream.of(new Output(writeToString1(sbgn)));
	}

	@Procedure(name = "ReadGraphFromDb", mode = Mode.READ)
	public Stream<Output> ReadGraphFromDb() throws JAXBException {

		return Stream.of(new Output(ReadGraph()));
	}

	@Procedure(name = "Neighbors", mode = Mode.WRITE)
	public Stream<Output> Neighbors(@Name("genesList") String geneList, @Name("limit") double limit)
			throws JAXBException {
		if (geneList == null) {
			return null;
		}

		return Stream.of(new Output(neighborsBFS(geneList, limit)));
	}

	@Procedure(name = "StreamHighlight", mode = Mode.WRITE)
	public Stream<Output> StreamHighlight(@Name("genesList") String geneList, @Name("limit") double limit,
			@Name("dir") double dir) throws JAXBException {
		if (geneList == null) {
			return null;
		}

		return Stream.of(new Output(stream(geneList, limit, dir)));
	}

	@Procedure(name = "StreamPaths", mode = Mode.WRITE)
	public Stream<Output> StreamPaths(@Name("genesList") String geneList, @Name("limit") double limit,
			@Name("dir") double dir) throws JAXBException {
		if (geneList == null) {
			return null;
		}

		return Stream.of(new Output(stream2(geneList, limit, dir)));
	}

	@Procedure(name = "PathsBetween", mode = Mode.WRITE)
	public Stream<Output> PathsBetween(@Name("genesList") String genesList,
			@Name("genesListTarget") String genesListTarget, @Name("limit") double limit,
			@Name("addition") double addition) throws JAXBException {
		if (genesList == null) {
			return null;
		}
		if (genesListTarget == null) {
			return null;
		}

		return Stream.of(new Output(PathsBetweenFunc(genesList, genesListTarget, limit, addition)));
	}

	@Procedure(name = "GOI", mode = Mode.WRITE)
	public Stream<Output> GOI(@Name("genesList") String genesList, @Name("limit") double limit,
			@Name("direction") double direction) throws JAXBException {
		if (genesList == null) {
			return null;
		}

		return Stream.of(new Output(GoIfunc(genesList, limit, direction)));
	}

	private String stream(String genesList, double limita, double direction) throws JAXBException {

		ArrayList<StreamObject> streamObjList = new ArrayList<StreamObject>();

		String queryM = "match (a) where   toLower(a.label) in {lists}  return  collect(a.id) as idlist";
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("lists", genesList.toLowerCase().split(" "));

		Result result = db.execute(queryM, parameters);

		Set<Object> idList = new HashSet<Object>();
		while (result.hasNext()) {
			Map<String, Object> row = result.next();
			idList.addAll((List<Object>) row.get("idlist"));
		}

		idList.forEach(idListItem -> {

			String queryM2 = "match (a) where a.id = {id}  optional match (a)-[:resideIn*]-(c) where not c:state_variable  return  collect(a.id) as idlist, collect(c.id) as cidlist";

			Map<String, Object> parameterse = new HashMap<String, Object>();
			parameterse.put("id", idListItem);

			Result resulte = db.execute(queryM2, parameterse);

			Set<Object> idListWithSiblings = new HashSet<Object>();
			while (resulte.hasNext()) {
				Map<String, Object> row = resulte.next();
				idListWithSiblings.addAll((List<Object>) row.get("idlist"));
				idListWithSiblings.addAll((List<Object>) row.get("cidlist"));
			}

			StreamObject strObj = new StreamObject();
			Set<Object> FreshIdListToIterate = new HashSet<Object>();
			Set<Object> OldIdListToIterate = new HashSet<Object>();
			FreshIdListToIterate.addAll(idListWithSiblings);
			int limit = (int) limita;
			while (limit > 0) {

				FreshIdListToIterate.removeAll(OldIdListToIterate);
				String query = traverse1Level(direction);

				Map<String, Object> parametersr = new HashMap<String, Object>();
				parametersr.put("lists", FreshIdListToIterate);

				Result result2 = db.execute(query, parametersr);
				OldIdListToIterate.addAll(FreshIdListToIterate);
				FreshIdListToIterate.clear();

				while (result2.hasNext()) {
					Map<String, Object> row = result2.next();

					Set<Node> nodebLists = new HashSet<Node>((List<Node>) row.get("blist"));
					// ************ CHECK IT
					strObj.pathList.addAll((List<Path>) row.get("p2List"));
					nodebLists.forEach(blistitem -> {

						if (blistitem != null) {

							blistitem.getLabels().forEach(blistitemlabel -> {

								if (blistitemlabel.name().equals("Port")) {

									String query3 = ifGlyphIsPort(direction);

									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));

										strObj.pathList.addAll((List<Path>) row3.get("pathList"));

									}

								} else if (blistitemlabel.name().equals("process")) {

									String query3 = ifGlyphIsProcess(direction);
									
									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));

										strObj.pathList.addAll((List<Path>) row3.get("pathList"));

									}
								} else if (blistitemlabel.name().equals("omitted_process")) {

									String query3 = ifGlyphIsOmittedProcess(direction);

									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));

										strObj.pathList.addAll((List<Path>) row3.get("pathList"));

									}

								} else if (blistitemlabel.name().equals("uncertain_process")) {

									String query3 = ifGlyphIsUncertainProcess(direction);

									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));

										strObj.pathList.addAll((List<Path>) row3.get("pathList"));

									}

								} else if (blistitemlabel.name().equals("association")) {

									String query3 = ifGlyphIsAssociation(direction);

									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
										strObj.pathList.addAll((List<Path>) row3.get("pathList"));

									}

								} else if (blistitemlabel.name().equals("dissociation")) {

									String query3 = ifGlyphIsDissociation(direction);
									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
										strObj.pathList.addAll((List<Path>) row3.get("pathList"));
									}
								}

								else {
									FreshIdListToIterate.add(blistitem.getProperty("id"));
									strObj.pathList.add((Path) row.get("p"));
									//TODO
								}

								if (blistitemlabel.name().equals("complex")) {

									String query3 = "match  (c:complex) where c.id = {id}  optional match p1= (c)-[:resideIn*]-(a)  return  [p1] as pathList";

									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										strObj.pathList.addAll((List<Path>) row3.get("pathList"));

									}
								}

							});

						}
					});

				}
				limit--;
			}

			streamObjList.add(strObj);
		});

		streamObjList.forEach(streamItem -> {

			streamItem.pathList.forEach(pathItem -> {

				if (pathItem != null) {
					pathItem.nodes().forEach(nodeItem -> {
						if (nodeItem != null)
							streamItem.nodesList.putIfAbsent(nodeItem.getProperty("id").toString(), nodeItem);

					});
				}

			});
		});

		Set<Node> finalNodes = new HashSet<Node>();

		if(streamObjList.size()>0)		
		finalNodes.addAll(streamObjList.get(0).nodesList.values());

		streamObjList.forEach(streamItem -> {

			finalNodes.retainAll(streamItem.nodesList.values());
		});
		String strforhighlight = "";

		Iterator<Node> it = finalNodes.iterator();
		while (it.hasNext()) {

			String ihd = (String) it.next().getProperty("id");

			String hg = "[id='" + ihd + "'],";
			strforhighlight += hg;
		}
		
		if(strforhighlight.length()>0)
		strforhighlight = strforhighlight.substring(0, strforhighlight.length() - 1);

		return strforhighlight;
	}

	private String traverse1Level(double direction) {
		String query = "";

		if (direction == 0) {
			query = "match p= (a)--(b) where a.id in {lists} optional match p2 = (b)-[:resideIn*]-(c) return [b, c] as blist, [p , p2] as p2List";
		} else if (direction == 1) {
			query = "match p= (a)-[]->(b) where a.id in {lists} optional match p2 = (b)-[:resideIn*]-(c) return [b, c] as blist, [p , p2] as p2List";
		} else if (direction == 2) {
			query = "match p= (a)<-[]-(b) where a.id in {lists} optional match p2 = (b)-[:resideIn*]-(c) return [b, c] as blist, [p , p2] as p2List";
		}
		return query;
	}

	private String stream2(String genesList, double limita, double direction) throws JAXBException {

		Set<Node> nodesListPurify = new HashSet<Node>();
		Set<Relationship> relsListPurify = new HashSet<Relationship>();
		ArrayList<StreamObject> streamObjList = new ArrayList<StreamObject>();
		Glyph a;
		HashMap<String, Glyph> glist = new HashMap<String, Glyph>();
		HashMap<String, Port> portlist = new HashMap<String, Port>();
		org.sbgn.bindings.Map sbgnMap = new org.sbgn.bindings.Map();
		Set<String> set = new HashSet<>();
		Set<String> comprefList = new HashSet<String>();
		Set<Relationship> allRelations = new HashSet<Relationship>();

		Set<Node> nodeList = new HashSet<Node>();
		String queryM = "match (a) where toLower(a.label) in {lists}  return  collect(a.id) as idlist";

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("lists", genesList.toLowerCase().split(" "));

		Result result = db.execute(queryM, parameters);

		Set<Object> idList = new HashSet<Object>();
		while (result.hasNext()) {
			Map<String, Object> row = result.next();
			idList.addAll((List<Object>) row.get("idlist"));
		}

		idList.forEach(idListItem -> {

			String queryM2 = "match (a) where a.id = {id}  optional match (a)-[:resideIn*]-(c) where not c:state_variable  return  collect(a.id) as idlist, collect(c.id) as cidlist";

			Map<String, Object> parameterse = new HashMap<String, Object>();
			parameterse.put("id", idListItem);

			Result resulte = db.execute(queryM2, parameterse);

			Set<Object> idListWithSiblings = new HashSet<Object>();
			while (resulte.hasNext()) {
				Map<String, Object> row = resulte.next();
				idListWithSiblings.addAll((List<Object>) row.get("idlist"));
				idListWithSiblings.addAll((List<Object>) row.get("cidlist"));
			}
			StreamObject strObj = new StreamObject();
			Set<Object> FreshIdListToIterate = new HashSet<Object>();
			Set<Object> OldIdListToIterate = new HashSet<Object>();
			FreshIdListToIterate.addAll(idListWithSiblings);
			int limit = (int) limita;
			while (limit > 0) {

				FreshIdListToIterate.removeAll(OldIdListToIterate);
				String query = traverse1Level(direction);

				Map<String, Object> parametersr = new HashMap<String, Object>();
				parametersr.put("lists", FreshIdListToIterate);

				Result result2 = db.execute(query, parametersr);
				OldIdListToIterate.addAll(FreshIdListToIterate);
				FreshIdListToIterate.clear();

				while (result2.hasNext()) {
					Map<String, Object> row = result2.next();

					Set<Node> nodebLists = new HashSet<Node>((List<Node>) row.get("blist"));
					// ************ CHECK IT
					strObj.pathList.addAll((List<Path>) row.get("p2List"));
					nodebLists.forEach(blistitem -> {

						if (blistitem != null) {

							blistitem.getLabels().forEach(blistitemlabel -> {

								if (blistitemlabel.name().equals("Port")) {

									String query3 = ifGlyphIsPort(direction);

									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));

										strObj.pathList.addAll((List<Path>) row3.get("pathList"));

									}

								} else if (blistitemlabel.name().equals("process")) {

									String query3 = ifGlyphIsProcess(direction);

									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));

										strObj.pathList.addAll((List<Path>) row3.get("pathList"));

									}
								} else if (blistitemlabel.name().equals("omitted_process")) {

									String query3 = ifGlyphIsOmittedProcess(direction);

									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
										strObj.pathList.addAll((List<Path>) row3.get("pathList"));

									}

								} else if (blistitemlabel.name().equals("uncertain_process")) {

									String query3 = ifGlyphIsUncertainProcess(direction);

									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();
										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
										strObj.pathList.addAll((List<Path>) row3.get("pathList"));
									}

								} else if (blistitemlabel.name().equals("association")) {

									String query3 = ifGlyphIsAssociation(direction);

									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
										strObj.pathList.addAll((List<Path>) row3.get("pathList"));
									}

								} else if (blistitemlabel.name().equals("dissociation")) {

									String query3 = ifGlyphIsDissociation(direction);
									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
										strObj.pathList.addAll((List<Path>) row3.get("pathList"));
									}

								}

								else {
									FreshIdListToIterate.add(blistitem.getProperty("id"));
									strObj.pathList.add((Path) row.get("p"));
								}

								if (blistitemlabel.name().equals("complex")) {

									String query3 = "match  (c:complex) where c.id = {id}  optional match p1= (c)-[:resideIn*]-(a)  return  [p1] as pathList";

									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();
										strObj.pathList.addAll((List<Path>) row3.get("pathList"));

									}
								}
							});
						}

					});

				}
				limit--;
			}

			streamObjList.add(strObj);
		});

		streamObjList.forEach(streamItem -> {

			streamItem.pathList.forEach(pathItem -> {

				if (pathItem != null) {

					if (pathItem.relationships() != null) {

						pathItem.relationships().forEach(relitem -> {
							if (relitem != null)
								allRelations.add(relitem);
						});

					}

					pathItem.nodes().forEach(nodeItem -> {
						if (nodeItem != null)
							streamItem.nodesList.putIfAbsent(nodeItem.getProperty("id").toString(), nodeItem);

					});
				}

			});
		});

		Set<Node> finalNodes = new HashSet<Node>();

		if(streamObjList.size()>0)
		finalNodes.addAll(streamObjList.get(0).nodesList.values());

		streamObjList.forEach(streamItem -> {

			finalNodes.retainAll(streamItem.nodesList.values());

		});

		String queryToPurify = "match (a) where a.id in {SourceidList} with a  optional match (b)  where  b in {finalNodes} with a, b optional match  p= (a)-[r*]-(b) where  all(rv IN  relationships(p)  WHERE rv in {relas})  return relationships(p) as rels, nodes(p) as nodes";
		Map<String, Object> parametersPurify = new HashMap<String, Object>();
		parametersPurify.put("SourceidList", idList);
		parametersPurify.put("finalNodes", finalNodes);
		parametersPurify.put("relas", allRelations);
		Result resultPurify = db.execute(queryToPurify, parametersPurify);

		while (resultPurify.hasNext()) {
			Map<String, Object> rowPurify = resultPurify.next();
			if ((List<Node>) rowPurify.get("nodes") != null)
				nodesListPurify.addAll((List<Node>) rowPurify.get("nodes"));

			if ((List<Relationship>) rowPurify.get("rels") != null)
				relsListPurify.addAll((List<Relationship>) rowPurify.get("rels"));

		}

		Traverse1AndExtractRelationsAndNodeForProcesses(db, nodesListPurify, relsListPurify);	
		retrieveNodesForFinalSBGNMap(db, nodesListPurify, glist, portlist, set);
		retrieveRelationsForResultSBGNMap(db, relsListPurify, glist, portlist, sbgnMap);
		glist.keySet().removeAll(set);
		for (Iterator<Glyph> iterator = glist.values().iterator(); iterator.hasNext();) {
			Glyph value = iterator.next();
			sbgnMap.getGlyph().add(value);
			
		}
		Sbgn sss = new Sbgn();
		sss.setMap(sbgnMap);
		String sbgnMl = writeToString(sss);
		return sbgnMl;
	}

	private String neighborsBFS(String genesList, double limita) throws JAXBException {

		HashMap<String, Node> nodesList = new HashMap<String, Node>();
		HashMap<String, Relationship> relsList = new HashMap<String, Relationship>();

		Glyph a;
		HashMap<String, Glyph> glist = new HashMap<String, Glyph>();
		HashMap<String, Port> portlist = new HashMap<String, Port>();
		org.sbgn.bindings.Map sbgnMap = new org.sbgn.bindings.Map();
		Set<String> set = new HashSet<>();
		Set<String> comprefList = new HashSet<String>();

		Set<Node> nodeList = new HashSet<Node>();

		String queryM = "match (a) where toLower(a.label) in {lists} optional match (a)-[:resideIn*]-(c)   return  collect(a.id) as idlist, collect(c.id) as cidlist";

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("lists", genesList.toLowerCase().split(" "));

		Result result = db.execute(queryM, parameters);

		Set<Path> pathList = new HashSet<Path>();
		Set<Object> idList = new HashSet<Object>();
		while (result.hasNext()) {
			Map<String, Object> row = result.next();
			idList.addAll((List<Object>) row.get("idlist"));
			idList.addAll((List<Object>) row.get("cidlist"));
		}

		idList.forEach(idListItem -> {

			Set<Object> FreshIdListToIterate = new HashSet<Object>();
			Set<Object> OldIdListToIterate = new HashSet<Object>();
			FreshIdListToIterate.add(idListItem);
			int limit = (int) limita;
			while (limit > 0) {
				FreshIdListToIterate.removeAll(OldIdListToIterate);

				String query = "match p= (a)--(b)  where a.id in {lists} optional match p2 = (b)-[:resideIn*]-(c) return [b, c] as blist, [p , p2] as p2List";
				Map<String, Object> parametersr = new HashMap<String, Object>();
				parametersr.put("lists", FreshIdListToIterate);

				Result result2 = db.execute(query, parametersr);
				OldIdListToIterate.addAll(FreshIdListToIterate);
				FreshIdListToIterate.clear();

				while (result2.hasNext()) {
					Map<String, Object> row = result2.next();
					// TODO diger proclara uygula
					pathList.addAll((List<Path>) row.get("p2List"));
					Set<Node> nodebLists = new HashSet<Node>((List<Node>) row.get("blist"));
					nodebLists.forEach(blistitem -> {

						if (blistitem != null) {

							blistitem.getLabels().forEach(blistitemlabel -> {

								if (blistitemlabel.name().equals("Port")) {

									String query3 = "match (po:Port) where po.id = {id}  optional match p1= (po)--(pr)--(:Port)--(a) where  pr:dissociation or  pr:process or pr:association or pr:omitted_process or pr:uncertain_process optional match p4=(a)-[:resideIn*]-(m) optional match p2 = (po)--(:process)--(b) where not b:Port optional match p7= (po)--(:omitted_process)--(f) where not f:Port optional match p8= (po)--(:uncertain_process)--(g) where not g:Port optional match p5=(b)-[:resideIn*]-(n) optional match p3 = (po)--(c) where not c:process and not c:omitted_process and not c:uncertain_process and not c:association  and not c:dissociation optional match p6=(c)-[:resideIn*]-(v) return [a.id,b.id, c.id, f.id, g.id, m.id, n.id,v.id] as idList, [p1,p2,p3,p4,p5,p6,p7,p8] as pathList";
									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
										pathList.addAll((List<Path>) row3.get("pathList"));

									}

								} else if (blistitemlabel.name().equals("process")) {

									String query3 = "match (p:process) where p.id = {id} optional match p1= (p)--(:Port)--(a) optional match p3=(a)-[:resideIn*]-(m) optional match p2=(p)--(b) where not b:Port optional match p4=(b)-[:resideIn*]-(n) return [a.id, b.id, m.id, n.id] as idList,  [p1,p2,p3,p4] as pathList";
									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));

										pathList.addAll((List<Path>) row3.get("pathList"));

									}
								} else if (blistitemlabel.name().equals("omitted_process")) {

									String query3 = "match (p:omitted_process) where p.id = {id} optional match p1= (p)--(:Port)--(a) optional match p3=(a)-[:resideIn*]-(m) optional match p2=(p)--(b) where not b:Port optional match p4=(b)-[:resideIn*]-(n) return [a.id, b.id, m.id, n.id] as idList,  [p1,p2,p3,p4] as pathList";
									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
										pathList.addAll((List<Path>) row3.get("pathList"));
									}

								} else if (blistitemlabel.name().equals("uncertain_process")) {

									String query3 = "match (p:uncertain_process) where p.id = {id} optional match p1= (p)--(:Port)--(a) optional match p3=(a)-[:resideIn*]-(m) optional match p2=(p)--(b) where not b:Port optional match p4=(b)-[:resideIn*]-(n) return [a.id, b.id, m.id, n.id] as idList,  [p1,p2,p3,p4] as pathList";
									Map<String, Object> parameters3 = new HashMap<String, Object>();
									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
										pathList.addAll((List<Path>) row3.get("pathList"));

									}

								} else if (blistitemlabel.name().equals("association")) {

									String query3 = "match (asc:association) where asc.id = {id}  optional match p1= (asc)--(:Port)--(a) optional match p2=(a)-[:resideIn*]-(m)  return [a.id, m.id] as idList, [p1,p2] as pathList";
									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
										pathList.addAll((List<Path>) row3.get("pathList"));
									}

								} else if (blistitemlabel.name().equals("dissociation")) {

									String query3 = "match (asc:dissociation) where asc.id = {id}  optional match p1= (asc)--(:Port)--(a) optional match p2=(a)-[:resideIn*]-(m)  return [a.id, m.id] as idList, [p1,p2] as pathList";
									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
										pathList.addAll((List<Path>) row3.get("pathList"));

									}
								}

								else {
									FreshIdListToIterate.add(blistitem.getProperty("id"));
									pathList.add((Path) row.get("p"));
								}

								if (blistitemlabel.name().equals("complex")) {

									String query3 = "match  (c:complex) where c.id = {id}  optional match p1= (c)-[:resideIn*]-(a)  return  [p1] as pathList";
									Map<String, Object> parameters3 = new HashMap<String, Object>();
									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();
										pathList.addAll((List<Path>) row3.get("pathList"));

									}
								}
							});
						}
					});
				}
				limit--;
			}

		});

		pathList.forEach(pathitem -> {

			if (pathitem != null) {
				if (pathitem.nodes() != null) {
					pathitem.nodes().forEach(nodeitem -> {
						if (nodeitem != null)
							nodesList.putIfAbsent(nodeitem.getProperty("id").toString(), nodeitem);
					});
				}

				if (pathitem.relationships() != null) {

					pathitem.relationships().forEach(relitem -> {
						if (relitem != null)
							relsList.putIfAbsent(relitem.getProperty("aid").toString(), relitem);
					});
				}
			}

		});
		Set<Node> nodesListPurify = new HashSet<Node>();
		Set<Relationship> relsListPurify = new HashSet<Relationship>();
		nodesListPurify.addAll(nodesList.values());
		relsListPurify.addAll(relsList.values());
		
		Traverse1AndExtractRelationsAndNodeForProcesses(db, nodesListPurify, relsListPurify);	
		retrieveNodesForFinalSBGNMap(db, nodesListPurify, glist, portlist, set);
		retrieveRelationsForResultSBGNMap(db, relsListPurify, glist, portlist, sbgnMap);
		
		glist.keySet().removeAll(set);
		for (Iterator<Glyph> iterator = glist.values().iterator(); iterator.hasNext();) {
			Glyph value = iterator.next();
			sbgnMap.getGlyph().add(value);			
		}
		Sbgn sss = new Sbgn();
		sss.setMap(sbgnMap);
		String sbgnMl = writeToString(sss);
		return sbgnMl;
	}

	private String PathsBetweenFunc(String genesList, String genesListTarget, double limita, double addition)
			throws JAXBException {
				int direction = 0;

				HashMap<String, Glyph> glist = new HashMap<String, Glyph>();
				HashMap<String, Port> portlist = new HashMap<String, Port>();
				org.sbgn.bindings.Map sbgnMap = new org.sbgn.bindings.Map();
				Set<String> set = new HashSet<>();

				Set<Node> nodesListPurify = new HashSet<Node>();
				Set<Relationship> relsListPurify = new HashSet<Relationship>();

				String queryT = "match (a) where toLower(a.label) in {lists}  optional match (a)-[:resideIn*]-(c)   return  collect(a.id) as idlist, collect(c.id) as cidlist";

				Map<String, Object> parametersT = new HashMap<String, Object>();
				parametersT.put("lists", genesListTarget.toLowerCase().split(" "));

				Result resultT = db.execute(queryT, parametersT);

				Set<Object> idListTarget = new HashSet<Object>();
				while (resultT.hasNext()) {
					Map<String, Object> row = resultT.next();
					idListTarget.addAll((List<Object>) row.get("idlist"));
					idListTarget.addAll((List<Object>) row.get("cidlist"));
				}

				String queryM = "match (a) where toLower(a.label) in {lists}   optional match (a)-[:resideIn*]-(c)   return  collect(a.id) as idlist, collect(c.id) as cidlist";

				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("lists", genesList.toLowerCase().split(" "));

				Result result = db.execute(queryM, parameters);

				Set<Object> idList = new HashSet<Object>();
				while (result.hasNext()) {
					Map<String, Object> row = result.next();
					idList.addAll((List<Object>) row.get("idlist"));
					idList.addAll((List<Object>) row.get("cidlist"));
				}

				int limit = (int) limita;

				double shortestlength = 9999;
				Set<PathsBetweenRoot> PathsBetweenRootList = new HashSet<PathsBetweenRoot>();

				for (Object idListItem : idList) {

					PathsBetweenRoot root = new PathsBetweenRoot(idListItem.toString(), 0);

					String queryM2 = "match (a) where a.id = {id}  optional match (a)-[:resideIn*]-(c) where not c:state_variable  return  collect(a.id) as idlist, collect(c.id) as cidlist";

					Map<String, Object> parameterse = new HashMap<String, Object>();
					parameterse.put("id", idListItem);

					Result resulte = db.execute(queryM2, parameterse);

					Set<Object> idListWithSiblings = new HashSet<Object>();
					while (resulte.hasNext()) {
						Map<String, Object> row = resulte.next();
						idListWithSiblings.addAll((List<Object>) row.get("idlist"));
					}

					root.FreshIdListToIterate.addAll(idListWithSiblings);
					PathListITem rootItem = new PathListITem(idListItem.toString() + ":0.0", idListItem.toString(), 0);
					rootItem.pathNodeIdList.add(idListItem.toString());
					AddToFreshPidTupleList(root.FreshIdMap, idListWithSiblings, "");
					root.PathListContainer.add(rootItem);

					PathsBetweenRootList.add(root);
				}

				double count = 0;

				while (count < limita && count < shortestlength + addition) {

					for (PathsBetweenRoot root : PathsBetweenRootList) {

						root.FreshIdListToIterate.removeAll(root.OldIdListToIterate);

						String query = "match p= (a)--(b)  where a.id in {lists} optional match p2 = (b)-[:resideIn*]-(c) return a.id as pid, [b, c] as blist, [p , p2] as p2List , [b.id, c.id] as bidlist";
						Map<String, Object> parametersr = new HashMap<String, Object>();
						parametersr.put("lists", root.FreshIdListToIterate);

						Result result2 = db.execute(query, parametersr);
						root.OldIdListToIterate.addAll(root.FreshIdListToIterate);
						root.FreshIdListToIterate.clear();

						while (result2.hasNext()) {
							Map<String, Object> row = result2.next();

							String pid = (String) row.get("pid");

							// TODO diger proclara uygula
							root.pathList.addAll((List<Path>) row.get("p2List"));
							Set<Node> nodebLists = new HashSet<Node>((List<Node>) row.get("blist"));

							for (Node blistitem : nodebLists) {

								if (blistitem != null) {

									blistitem.getLabels().forEach(blistitemlabel -> {

										if (blistitemlabel.name().equals("Port")) {

											String query3 = ifGlyphIsPort(direction);
											Map<String, Object> parameters3 = new HashMap<String, Object>();

											parameters3.put("id", blistitem.getProperty("id"));

											Result result3 = db.execute(query3, parameters3);

											while (result3.hasNext()) {
												Map<String, Object> row3 = result3.next();

												root.FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
												AddToFreshPidTupleList(root.FreshIdMap, (List<Object>) row3.get("idList"), pid);
												root.pathList.addAll((List<Path>) row3.get("pathList"));
											}

										} else if (blistitemlabel.name().equals("process")) {

											String query3 = ifGlyphIsProcess(direction);

											Map<String, Object> parameters3 = new HashMap<String, Object>();

											parameters3.put("id", blistitem.getProperty("id"));

											Result result3 = db.execute(query3, parameters3);

											while (result3.hasNext()) {
												Map<String, Object> row3 = result3.next();

												root.FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
												AddToFreshPidTupleList(root.FreshIdMap, (List<Object>) row3.get("idList"), pid);
												root.pathList.addAll((List<Path>) row3.get("pathList"));

											}
										} else if (blistitemlabel.name().equals("omitted_process")) {

											String query3 = ifGlyphIsOmittedProcess(direction);

											Map<String, Object> parameters3 = new HashMap<String, Object>();

											parameters3.put("id", blistitem.getProperty("id"));
											Result result3 = db.execute(query3, parameters3);

											while (result3.hasNext()) {
												Map<String, Object> row3 = result3.next();
												AddToFreshPidTupleList(root.FreshIdMap, (List<Object>) row3.get("idList"), pid);
												root.FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
												root.pathList.addAll((List<Path>) row3.get("pathList"));
											}

										} else if (blistitemlabel.name().equals("uncertain_process")) {

											String query3 = ifGlyphIsUncertainProcess(direction);

											Map<String, Object> parameters3 = new HashMap<String, Object>();

											parameters3.put("id", blistitem.getProperty("id"));
											Result result3 = db.execute(query3, parameters3);

											while (result3.hasNext()) {
												Map<String, Object> row3 = result3.next();
												AddToFreshPidTupleList(root.FreshIdMap, (List<Object>) row3.get("idList"), pid);
												root.FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
												root.pathList.addAll((List<Path>) row3.get("pathList"));

											}

										} else if (blistitemlabel.name().equals("association")) {

											String query3 = ifGlyphIsAssociation(direction);

											Map<String, Object> parameters3 = new HashMap<String, Object>();

											parameters3.put("id", blistitem.getProperty("id"));

											Result result3 = db.execute(query3, parameters3);

											while (result3.hasNext()) {
												Map<String, Object> row3 = result3.next();
												AddToFreshPidTupleList(root.FreshIdMap, (List<Object>) row3.get("idList"), pid);
												root.FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
												root.pathList.addAll((List<Path>) row3.get("pathList"));

											}

										} else if (blistitemlabel.name().equals("dissociation")) {

											String query3 = ifGlyphIsDissociation(direction);

											Map<String, Object> parameters3 = new HashMap<String, Object>();

											parameters3.put("id", blistitem.getProperty("id"));

											Result result3 = db.execute(query3, parameters3);

											while (result3.hasNext()) {
												Map<String, Object> row3 = result3.next();
												AddToFreshPidTupleList(root.FreshIdMap, (List<Object>) row3.get("idList"), pid);
												root.FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
												root.pathList.addAll((List<Path>) row3.get("pathList"));
											}
										}

										else {
										//	root.FreshIdListToIterate.add(blistitem.getProperty("id"));
										//	List<Object> lst = new ArrayList<Object>();
										//	lst.add(blistitem.getProperty("id"));
										//	AddToFreshPidTupleList(root.FreshIdMap, lst, pid);
											
											AddToFreshPidTupleList(root.FreshIdMap, (List<Object>) row.get("bidlist"), pid);
											root.FreshIdListToIterate.addAll((List<Object>) row.get("bidlist"));
											root.pathList.addAll((List<Path>) row.get("p2List"));
										}

										if (blistitemlabel.name().equals("complex")) {

											String query3 = "match  (c:complex) where c.id = {id}  optional match p1= (c)-[:resideIn*]-(a)   unwind nodes(p1) as p1Node with DISTINCT p1Node, p1  return collect(p1Node.id) as idList,  [p1] as pathList";
											Map<String, Object> parameters3 = new HashMap<String, Object>();

											parameters3.put("id", blistitem.getProperty("id"));

											Result result3 = db.execute(query3, parameters3);

											while (result3.hasNext()) {
												Map<String, Object> row3 = result3.next();
												root.FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
												AddToFreshPidTupleList(root.FreshIdMap, (List<Object>) row3.get("idList"), pid);
												root.pathList.addAll((List<Path>) row3.get("pathList"));

											}

										}
									});
								}

							}
						}
						addPathItemToPathListContainerOfRoot(count, root);

						if (shortestlength == 9999 && !Collections.disjoint(root.FreshIdListToIterate, idListTarget)) {
							shortestlength = count + 1;
						}

					}
					count++;
				}

				for (PathsBetweenRoot rootItem : PathsBetweenRootList) {

					for (PathListITem ptlItem : rootItem.PathListContainer) {
						//System.out.println(ptlItem.pathNodeIdList);
						if (idListTarget.contains(ptlItem.endNodeId)) {

							String queryToGetCompoundNodes = "match (a) where a.id in {SourceidList} optional match p1= (a)-[:resideIn*]-(b) unwind nodes(p1) as p1Node   return collect(p1Node.id) as cmpndNodes";

							Map<String, Object> parametersGetCompoundNodes = new HashMap<String, Object>();

							parametersGetCompoundNodes.put("SourceidList", ptlItem.pathNodeIdList);
							Set<String> pathNodeIdList = new HashSet<String>();
							Result resultGetCompoundNodes = db.execute(queryToGetCompoundNodes, parametersGetCompoundNodes);
							Set<Node> NodeListForPurifyQuery = new HashSet<Node>();
							while (resultGetCompoundNodes.hasNext()) {
								Map<String, Object> rowGetCompoundNodes = resultGetCompoundNodes.next();
								pathNodeIdList.addAll((List<String>) rowGetCompoundNodes.get("cmpndNodes"));
							}
							pathNodeIdList.addAll(ptlItem.pathNodeIdList);
							HashMap<String, Node> nodesList = new HashMap<String, Node>();
							rootItem.pathList.forEach(pathItem -> {

								if (pathItem != null) {

									pathItem.nodes().forEach(nodeItem -> {
										if (nodeItem != null) {

											for (org.neo4j.graphdb.Label lbl : nodeItem.getLabels()) {

												if (lbl.name().equals("macromolecule") ) {
													String nodeID = nodeItem.getProperty("id").toString();
													if (pathNodeIdList.contains(nodeID)) {
														nodesList.putIfAbsent(nodeID, nodeItem);
													}
												} 
												
												else if ( lbl.name().equals("complex")) {
													String nodeID = nodeItem.getProperty("id").toString();
											
													
													if (pathNodeIdList.contains(nodeID)) {
														nodesList.putIfAbsent(nodeID, nodeItem);
													}
												} 
												else {
													nodesList.putIfAbsent(nodeItem.getProperty("id").toString(), nodeItem);
												}
											}

										}

									});
								}
							});

							String queryToPurify = "match (a) where a.id = {Sourceid} with a  optional match (b)  where  b.id in {finalNodes} with a, b optional match  p= (a)-[r*]-(b)  where  all(rv IN  nodes(p)  WHERE rv in {nodes} )  unwind nodes(p) as pNode with DISTINCT pNode, p optional match p2= (pNode)-[:resideIn*]-(ac)  return [p,p2] as pathList";

							Map<String, Object> parametersPurify = new HashMap<String, Object>();

							parametersPurify.put("Sourceid", rootItem.nodeId);
							parametersPurify.put("finalNodes", idListTarget);
							parametersPurify.put("nodes", nodesList.values());

							Result resultPurify = db.execute(queryToPurify, parametersPurify);
							Set<Path> pathListForFinal = new HashSet<Path>();
							while (resultPurify.hasNext()) {
								Map<String, Object> rowPurify = resultPurify.next();
								pathListForFinal.addAll((List<Path>) rowPurify.get("pathList"));
							}

							pathListForFinal.forEach(pathitem -> {

								if (pathitem != null) {
									if (pathitem.nodes() != null) {
										pathitem.nodes().forEach(nodeitem -> {
											if (nodeitem != null)
												nodesListPurify.add(nodeitem);
										});
									}

									if (pathitem.relationships() != null) {

										pathitem.relationships().forEach(relitem -> {
											if (relitem != null)
												relsListPurify.add(relitem);
										});
									}
								}
							});

						}
					}
				}

				Traverse1AndExtractRelationsAndNodeForProcesses(db, nodesListPurify, relsListPurify);	
				retrieveNodesForFinalSBGNMap(db, nodesListPurify, glist, portlist, set);
				retrieveRelationsForResultSBGNMap(db, relsListPurify, glist, portlist, sbgnMap);

				glist.keySet().removeAll(set);
				for (Iterator<Glyph> iterator = glist.values().iterator(); iterator.hasNext();) {
					Glyph value = iterator.next();
					sbgnMap.getGlyph().add(value);
				}

				Sbgn sss = new Sbgn();
				sss.setMap(sbgnMap);
				String sbgnMl = writeToString(sss);
				return sbgnMl;}

	public String GoIfunc(String genesList, double limita, double direction) throws JAXBException {

		int limit;

		Set<Node> nodesListPurify = new HashSet<Node>();
		Set<Relationship> relsListPurify = new HashSet<Relationship>();
		ArrayList<GOINodeContainer> GOINodeContainerList = new ArrayList<GOINodeContainer>();

		Set<Relationship> allRelations = new HashSet<Relationship>();

		Set<Node> nodeList = new HashSet<Node>();

		String queryM = "match (a) where toLower(a.label) in {lists} return  collect(a.id) as idlist";

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("lists", genesList.toLowerCase().split(" "));

		Result result = db.execute(queryM, parameters);

		// Set<Path> pathList = new HashSet<Path>();
		Set<Object> idList = new HashSet<Object>();
		while (result.hasNext()) {
			Map<String, Object> row = result.next();
			idList.addAll((List<Object>) row.get("idlist"));
		}

		for (Object idListItem : idList) {

			// GOINodeContainer
			GOINodeContainer gOINodeContainer = new GOINodeContainer();
			gOINodeContainer.rootId = (String) idListItem;
			gOINodeContainer.GOINodeObjectList.put((String) idListItem, new GOINodeObject((String) idListItem, 0));
			String queryM2 = "match (a) where a.id = {id}  optional match (a)-[:resideIn*]-(c) where not c:state_variable  return  collect(a.id) as idlist, collect(c.id) as cidlist";

			Map<String, Object> parameterse = new HashMap<String, Object>();
			parameterse.put("id", idListItem);

			Result resulte = db.execute(queryM2, parameterse);

			Set<Object> idListWithSiblings = new HashSet<Object>();
			while (resulte.hasNext()) {
				Map<String, Object> row = resulte.next();
				idListWithSiblings.addAll((List<Object>) row.get("idlist"));
				idListWithSiblings.addAll((List<Object>) row.get("cidlist"));
			}
			
			for (Object item : idListWithSiblings){
				gOINodeContainer.GOINodeObjectList.put((String)item, new GOINodeObject((String)item, 0));						
			}
			Set<Object> FreshIdListToIterate = new HashSet<Object>();
			Set<Object> OldIdListToIterate = new HashSet<Object>();
			FreshIdListToIterate.addAll(idListWithSiblings);
			limit = (int) limita;
			while (limit > 0) {

				FreshIdListToIterate.removeAll(OldIdListToIterate);
				String query = traverse1Level(direction);

				Map<String, Object> parametersr = new HashMap<String, Object>();
				parametersr.put("lists", FreshIdListToIterate);

				Result result2 = db.execute(query, parametersr);
				OldIdListToIterate.addAll(FreshIdListToIterate);
				FreshIdListToIterate.clear();

				while (result2.hasNext()) {
					Map<String, Object> row = result2.next();

					Set<Node> nodebLists = new HashSet<Node>((List<Node>) row.get("blist"));
					// ************ CHECK IT
					gOINodeContainer.pathList.addAll((List<Path>) row.get("p2List"));

					for (Node blistitem : nodebLists) {

						if (blistitem != null) {

							for (org.neo4j.graphdb.Label blistitemlabel : blistitem.getLabels()) {

								if (blistitemlabel.name().equals("Port")) {

									String query3 = ifGlyphIsPort(direction);

									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
										gOINodeContainer.pathListforlabelallglyphs
												.addAll((List<Path>) row3.get("pathList"));
										double depth3 = limita - limit;
										GoiNodePathExtract(gOINodeContainer, (List<String>) row3.get("idList"),
												(List<Path>) row3.get("pathList"), depth3);
									}

								} else if (blistitemlabel.name().equals("process")) {

									String query3 = ifGlyphIsProcess(direction);

									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
										gOINodeContainer.pathListforlabelallglyphs
												.addAll((List<Path>) row3.get("pathList"));
										double depth3 = limita - limit;
										GoiNodePathExtract(gOINodeContainer, (List<String>) row3.get("idList"),
												(List<Path>) row3.get("pathList"), depth3);

									}
								} else if (blistitemlabel.name().equals("omitted_process")) {

									String query3 = ifGlyphIsOmittedProcess(direction);
									Map<String, Object> parameters3 = new HashMap<String, Object>();
									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();

										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
										gOINodeContainer.pathListforlabelallglyphs
												.addAll((List<Path>) row3.get("pathList"));
										double depth3 = limita - limit;
										GoiNodePathExtract(gOINodeContainer, (List<String>) row3.get("idList"),
												(List<Path>) row3.get("pathList"), depth3);

									}

								} else if (blistitemlabel.name().equals("uncertain_process")) {

									String query3 = ifGlyphIsUncertainProcess(direction);

									Map<String, Object> parameters3 = new HashMap<String, Object>();
									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();
										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
										gOINodeContainer.pathListforlabelallglyphs
												.addAll((List<Path>) row3.get("pathList"));
										double depth3 = limita - limit;
										GoiNodePathExtract(gOINodeContainer, (List<String>) row3.get("idList"),
												(List<Path>) row3.get("pathList"), depth3);
									}

								} else if (blistitemlabel.name().equals("association")) {

									String query3 = ifGlyphIsAssociation(direction);

									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();
										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
										gOINodeContainer.pathListforlabelallglyphs
												.addAll((List<Path>) row3.get("pathList"));

										double depth3 = limita - limit;
										GoiNodePathExtract(gOINodeContainer, (List<String>) row3.get("idList"),
												(List<Path>) row3.get("pathList"), depth3);

									}

								} else if (blistitemlabel.name().equals("dissociation")) {

									String query3 = ifGlyphIsDissociation(direction);
									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();
										gOINodeContainer.pathListforlabelallglyphs
												.addAll((List<Path>) row3.get("pathList"));
										FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
										double depth3 = limita - limit;
										GoiNodePathExtract(gOINodeContainer, (List<String>) row3.get("idList"),
												(List<Path>) row3.get("pathList"), depth3);
									}

								}

								else {
									FreshIdListToIterate.add(blistitem.getProperty("id"));

									List<String> tempforuseasparamater = new ArrayList<String>();
									tempforuseasparamater.add((String) blistitem.getProperty("id"));

									List<Path> tempforuseasparamater2 = new ArrayList<Path>();
									tempforuseasparamater2.add((Path) row.get("p"));
									gOINodeContainer.pathListforlabelallglyphs.add((Path) row.get("p"));
									gOINodeContainer.pathListforlabelallglyphs.addAll((List<Path>) row.get("p2List"));
									double depth3 = limita - limit;
									GoiNodePathExtract(gOINodeContainer, tempforuseasparamater, tempforuseasparamater2,
											depth3);

								}

								if (blistitemlabel.name().equals("complex")) {

									String query3 = "match  (c:complex) where c.id = {id}  optional match p1= (c)-[:resideIn*]-(a)  return  [p1] as pathList";

									Map<String, Object> parameters3 = new HashMap<String, Object>();

									parameters3.put("id", blistitem.getProperty("id"));

									Result result3 = db.execute(query3, parameters3);

									while (result3.hasNext()) {
										Map<String, Object> row3 = result3.next();
										gOINodeContainer.pathList.addAll((List<Path>) row3.get("pathList"));
										gOINodeContainer.pathListforlabelallglyphs
												.addAll((List<Path>) row3.get("pathList"));
									}

								}
							}
						}

					}

				}
				limit--;

				double depth2 = limita - limit;

				FreshIdListToIterate.forEach(FreshIdListItem -> {
					gOINodeContainer.GOINodeObjectList.putIfAbsent((String) FreshIdListItem,
							new GOINodeObject((String) FreshIdListItem, depth2));					
				});

				gOINodeContainer.pathListforlabelallglyphs.forEach(pathitem -> {
					if (pathitem != null) {
						pathitem.nodes().forEach(nodeitem -> {
							if (nodeitem != null) {
								String nodeid = (String) nodeitem.getProperty("id");
								gOINodeContainer.GOINodeObjectList.putIfAbsent(nodeid,
										new GOINodeObject(nodeid, depth2 - 0.5));

							}
						});

					}

				});
			}

			GOINodeContainerList.add(gOINodeContainer);
		}

		for (int x = 0; x < GOINodeContainerList.size(); x++) {

			for (int y = 0; y < GOINodeContainerList.size(); y++) {

				if (GOINodeContainerList.get(x) != GOINodeContainerList.get(y)) {
					Object[] keyArray = GOINodeContainerList.get(x).GOINodeObjectList.keySet().toArray();
					for (int z = 0; z < keyArray.length; z++) {
						Object key = keyArray[z];
						GOINodeObject gobj = GOINodeContainerList.get(y).GOINodeObjectList.get(key);

						if (gobj != null) {

							if (gobj.dist + GOINodeContainerList.get(x).GOINodeObjectList.get(key).dist <= limita) {

								GOINodeContainerList.get(x).finalIds.add((String) key);
								if (GOINodeContainerList.get(x).listOfpathslist.get(key) != null)
									GOINodeContainerList.get(x).pathList
											.addAll(GOINodeContainerList.get(x).listOfpathslist.get(key));
							}

						}

					}

				}

			}
		}

		GOINodeContainerList.forEach(gci -> {
			Set<Relationship> allRelationsgci = new HashSet<Relationship>();
			Set<Node> nodes = new HashSet<Node>();
			gci.pathList.forEach(pathitem -> {

				if (pathitem != null && pathitem.relationships() != null) {

					pathitem.relationships().forEach(relitem -> {
						if (relitem != null)
							allRelationsgci.add(relitem);
					});

				}

				if (pathitem != null && pathitem.nodes() != null) {

					pathitem.nodes().forEach(nodeitem -> {
						if (nodeitem != null && gci.finalIds.contains(nodeitem.getProperty("id"))) {
							nodes.add(nodeitem);
						}
					});
				}
			}

			);

			String queryToPurify = "match (a) where a.id = {SourceidList} with a  optional match (b)  where not b:Port and not b:process and b.id in {finalNodes} with a, b optional match  p= (a)-[r*]-(b) where  all(rv IN  nodes(p)  WHERE rv in {nodas} or rv.id in {SourceidList})   return relationships(p) as rels, nodes(p) as nodes";

			Map<String, Object> parametersPurify = new HashMap<String, Object>();

			parametersPurify.put("SourceidList", gci.rootId);
			parametersPurify.put("finalNodes", gci.finalIds);
			// parametersPurify.put("relas", allRelationsgci);
			parametersPurify.put("nodas", nodes);

			Result resultPurify = db.execute(queryToPurify, parametersPurify);

			while (resultPurify.hasNext()) {
				Map<String, Object> rowPurify = resultPurify.next();

				if ((List<Node>) rowPurify.get("nodes") != null)
					nodesListPurify.addAll((List<Node>) rowPurify.get("nodes"));

				if ((List<Relationship>) rowPurify.get("rels") != null)
					relsListPurify.addAll((List<Relationship>) rowPurify.get("rels"));
			}

		});

		HashMap<String, Glyph> glist = new HashMap<String, Glyph>();
		HashMap<String, Port> portlist = new HashMap<String, Port>();
		org.sbgn.bindings.Map sbgnMap = new org.sbgn.bindings.Map();
		Set<String> set = new HashSet<>();
		Traverse1AndExtractRelationsAndNodeForProcesses(db, nodesListPurify, relsListPurify);	
		retrieveNodesForFinalSBGNMap(db, nodesListPurify, glist, portlist, set);
		retrieveRelationsForResultSBGNMap(db, relsListPurify, glist, portlist, sbgnMap);
		glist.keySet().removeAll(set);
		for (Iterator<Glyph> iterator = glist.values().iterator(); iterator.hasNext();) {
			Glyph value = iterator.next();
			sbgnMap.getGlyph().add(value);
		}
		Sbgn sss = new Sbgn();
		sss.setMap(sbgnMap);
		String sbgnMl = writeToString(sss);
		return sbgnMl;

	}

	private void InsertArc(Arc a) {

		float startY = a.getStart().getX();
		float startX = a.getStart().getY();
		float endX = a.getEnd().getX();
		float endY = a.getEnd().getY();
		String aid = a.getId();

		String sourceid = "";

		if (a.getSource().getClass() == Glyph.class) {
			sourceid = FormatSourceOrTarget(((Glyph) a.getSource()).getId());
		} else {
			sourceid = FormatSourceOrTarget(((Port) a.getSource()).getId());
		}

		String targetid = "";

		if (a.getTarget().getClass() == Glyph.class) {
			targetid = FormatSourceOrTarget(((Glyph) a.getTarget()).getId());
		} else {

			targetid = FormatSourceOrTarget(((Port) a.getTarget()).getId());
		}

		String query = "MATCH (a) where  a.id= '" + sourceid + "'  Match (b) where b.id='" + targetid + "' Merge (a)-[:"
				+ a.getClazz().replaceAll(" ", "_")
				+ " {startY: {startY} , startX: {startX}, endX: {endX} , endY: {endY} , aid: {aid} } ]-> (b)";

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("startY", startY);
		parameters.put("startX", startX);
		parameters.put("endX", endX);
		parameters.put("endY", endY);
		parameters.put("aid", aid);

		db.execute(query, parameters);

	}

	private void InsertGlyph(Glyph g) {

		String label = "";
		if (g.getLabel() != null)
			label = g.getLabel().getText();

		String gid = g.getId();		
		float y = g.getBbox().getY();
		float x = g.getBbox().getX();
		float w = g.getBbox().getW();
		float h = g.getBbox().getH();

		String stateVal = "";
		if (g.getState() != null) {
			if (g.getState().getValue() != null)
				stateVal = g.getState().getValue();

		}

		String compRef = "notexist";
		if (g.getCompartmentRef() != null)
			compRef = ((Glyph) g.getCompartmentRef()).getId();
		String query = "MERGE (a:" + g.getClazz().replaceAll(" ", "_")
				+ "{label: {label} , id: {id}, x: {x}, y: {y}, w: {w},  h: {h}, compRef: {compRef}, stateVal: {stateVal} })";
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("label", label);
		parameters.put("id", gid);
		parameters.put("x", x);
		parameters.put("w", w);
		parameters.put("y", y);
		parameters.put("h", h);
		parameters.put("compRef", compRef);
		parameters.put("stateVal", stateVal);

		db.execute(query, parameters);

		if (g.getGlyph().size() > 0) {
			g.getGlyph().forEach(item -> InsertGlyph(item));
			g.getGlyph().forEach(item2 -> RelateToParent(g, item2));
		}
		if (g.getPort().size() > 0) {
			g.getPort().forEach(item -> InsertPort(item, g.getId()));
			// g.getPort().forEach(item2->RelatePParent(g, item2) );

		}
	}

	private void InsertPort(Port g, String parentId) {

		String label = "";

		label = "Port";

		String gid = g.getId();
		float y = g.getY();
		float x = g.getX();

		String query = "MERGE (a:Port" + "{x: {x} , y: {y} , id: {id}}) WITH a MATCH (p)  where p.id = '" + parentId
				+ "' Merge (p)-[:hasPort {aid: '999'}]->(a)";
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("id", gid);
		parameters.put("x", x);
		parameters.put("y", y);

		db.execute(query, parameters);

	}

	private void InsertBox(Bbox bbox, Glyph g) {
		if (bbox == null)
			return;

		float y = bbox.getY();
		float x = bbox.getX();
		float w = bbox.getW();
		float h = bbox.getH();
		String gid = g.getId();

		String query = "MATCH (g) where g.id = '" + gid
				+ "' Merge (g)-[:hasBbox]->(a:BBox  { y: {y} , x: {x} , h: {h} , w: {w}})";
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("y", y);
		parameters.put("x", x);
		parameters.put("h", h);
		parameters.put("w", w);
		db.execute(query, parameters);
	}

	private void RelateToParent(Glyph a, Glyph b) {
		String query = "MATCH (a) where  a.id= '" + a.getId() + "'  Match (b) where b.id='" + b.getId()
				+ "' Merge (a)<-[:resideIn {aid: '999'}]- (b)";
		db.execute(query);

	}

	public Sbgn readFromFile(File f) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance("org.sbgn.bindings");
		Unmarshaller unmarshaller = context.createUnmarshaller();
		Sbgn result = (Sbgn) unmarshaller.unmarshal(f);
		return result;
	}

	public Sbgn readFromString(String f) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance("org.sbgn.bindings");
		Unmarshaller unmarshaller = context.createUnmarshaller();
		StringReader reader = new StringReader(f);
		Sbgn result = (Sbgn) unmarshaller.unmarshal(reader);
		return result;
	}

	public static String writeToString1(Sbgn sbgn) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance("org.sbgn.bindings");
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		marshaller.marshal(sbgn, sw);
		return sw.toString();
	}

	public String FormatSourceOrTarget(String str) {
		// if(str.contains("."))
		// return str.substring(0,str.indexOf(".") );

		return str;
	}

	public static String writeToString(Sbgn sbgn) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance("org.sbgn.bindings");
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		marshaller.marshal(sbgn, sw);
		return sw.toString();
	}

	public String ReadGraph() throws JAXBException {

		Glyph a;
		HashMap<String, Glyph> glist = new HashMap<String, Glyph>();
		HashMap<String, Port> portlist = new HashMap<String, Port>();
		// Transaction tx = null;
		Set<String> set = new HashSet<>();

		org.sbgn.bindings.Map sbgnMap = new org.sbgn.bindings.Map();

		Result result = db.execute(
				"Match (g) where  not g:Port optional match (g)<-[:resideIn]-(gc) optional match (g)-[:hasPort]->(p:Port) return g, collect(gc) as childGlyph, collect(p) as ports order by g.id");
		while (result.hasNext()) {
			Map<String, Object> row = result.next();

			List<Node> ports = (List<Node>) row.get("ports");
			List<Node> childGlyphs = (List<Node>) row.get("childGlyph");

			Glyph glyph;
			String glyphId = (String) ((Node) row.get("g")).getProperty("id");
			if (glist.containsKey(glyphId))
				glyph = glist.get(glyphId);
			else {
				glyph = new Glyph();
			}

			glyph.setId(glyphId);

			glyph.setClazz(((Node) row.get("g")).getLabels().iterator().next().toString().replaceAll("_", " "));

			if (!((String) ((Node) row.get("g")).getProperty("compRef")).equals("notexist")) {
				Glyph glyphforCompartment = new Glyph();
				glyphforCompartment.setId((String) ((Node) row.get("g")).getProperty("compRef"));
				glyph.setCompartmentRef(glyphforCompartment);
			}
			Bbox bBox = new Bbox();
			float x = (float) ((Node) row.get("g")).getProperty("x");
			float y = (float) ((Node) row.get("g")).getProperty("y");
			float w = (float) ((Node) row.get("g")).getProperty("w");
			float h = (float) ((Node) row.get("g")).getProperty("h");
			bBox.setH((float) h);
			bBox.setW((float) w);
			bBox.setX((float) x);
			bBox.setY((float) y);

			glyph.setBbox(bBox);

			Label label = new Label();
			String lbl = (String) ((Node) row.get("g")).getProperty("label");
			label.setText(lbl);
			glyph.setLabel(label);

			State state = new State();
			String st = (String) ((Node) row.get("g")).getProperty("stateVal");
			state.setValue(st);
			glyph.setState(state);
			ports.forEach(item -> {
				Port port = new Port();
				float xPort = (float) item.getProperty("x");
				float yPort = (float) item.getProperty("y");
				String Pid = (String) item.getProperty("id");

				port.setId(Pid);
				port.setX((int) xPort);
				port.setY((int) yPort);

				glyph.getPort().add(port);
				portlist.put(Pid, port);

			});

			childGlyphs.forEach(item -> {

				Glyph childglyph;
				String childglyphId = (String) item.getProperty("id");

				if (glist.containsKey(childglyphId))
					childglyph = glist.get(childglyphId);
				else {
					childglyph = new Glyph();
				}
				childglyph.setId(childglyphId);
				childglyph.setClazz(item.getLabels().iterator().next().toString());
				Bbox childbBox = new Bbox();
				float cx = (float) item.getProperty("x");
				float cy = (float) item.getProperty("y");
				float cw = (float) item.getProperty("w");
				float ch = (float) item.getProperty("h");

				childbBox.setH((float) ch);
				childbBox.setW((float) cw);
				childbBox.setX((float) cx);
				childbBox.setY((float) cy);

				childglyph.setBbox(childbBox);

				Label childlabel = new Label();
				String clbl = (String) item.getProperty("label");
				childlabel.setText(clbl);
				childglyph.setLabel(childlabel);

				State cstate = new State();
				String cst = (String) item.getProperty("stateVal");
				cstate.setValue(cst);
				childglyph.setState(cstate);

				if (!((String) item.getProperty("compRef")).equals("notexist")) {

					Glyph cglyphforCompartment = new Glyph();
					cglyphforCompartment.setId((String) item.getProperty("compRef"));
					childglyph.setCompartmentRef(cglyphforCompartment);

				}

				glist.putIfAbsent(childglyph.getId(), childglyph);
				set.add(childglyph.getId());
				glyph.getGlyph().add(childglyph);

			});

			glist.putIfAbsent(glyph.getId(), glyph);
		}

		String query = "MATCH (a)-[r]->(b) where r.startX is not null return a.id as sid,b.id as tid, r.startX as xx,r.startY as yy, r.endX as ex, r.endY as ey, r.aid as id, type(r) as class";
	
		Result result2 = db.execute(query);

		while (result2.hasNext()) {
			Map<String, Object> row2 = result2.next();

			float startX = (float) row2.get("xx");
			float startY = (float) row2.get("yy");
			Start start = new Start();
			start.setX((float) startX);
			start.setY((float) startY);
			float endX = (float) row2.get("ex");
			float endY = (float) row2.get("ey");
			String aid = (String) row2.get("id");
			String clazz = (String) row2.get("class");
			clazz = clazz.replaceAll("_", " ");
			End end = new End();
			end.setX((float) endX);
			end.setY((float) endY);
			Arc arc = new Arc();
			arc.setClazz(clazz);
			arc.setId(aid);
			arc.setEnd(end);
			arc.setStart(start);
			if (glist.get(row2.get("sid")) != null) {
				arc.setSource(glist.get(row2.get("sid")));
			} else {
				arc.setSource(portlist.get(row2.get("sid")));
			}

			if (glist.get(row2.get("tid")) != null) {
				arc.setTarget(glist.get(row2.get("tid")));
			} else {
				arc.setTarget(portlist.get(row2.get("tid")));
			}
			sbgnMap.getArc().add(arc);
		}

		int cnts = 1;
		glist.keySet().removeAll(set);
		for (Iterator<Glyph> iterator = glist.values().iterator(); iterator.hasNext();) {
			Glyph value = iterator.next();
			sbgnMap.getGlyph().add(value);
			cnts++;
		}
		Sbgn sss = new Sbgn();
		sss.setMap(sbgnMap);
		String sbgnMl = writeToString1(sss);

		return sbgnMl;
	}

	private void retrieveNodesForFinalSBGNMap(GraphDatabaseService graphDb, Collection<Node> collection,
			HashMap<String, Glyph> glist, HashMap<String, Port> portlist, Set<String> set) {
		Set<String> comprefList = new HashSet<String>();
		String queryF = "Match (g) where not g:Port and  g in {nodes} optional match (g)<-[:resideIn]-(gc) optional match (g)-[:hasPort]->(p:Port) optional match (g)-[:resideIn]->(prnt) optional match (sblg) - [:resideIn] -> (prnt) where sblg <> g return g, collect(gc) as childGlyph, collect(p) as ports, collect(distinct prnt) as prntList, collect(sblg) as sblgList, count(prnt) as  prntcount  order by prntcount desc";

		Map<String, Object> parametersF = new HashMap<String, Object>();
		parametersF.put("nodes", collection);

		Result resultF = graphDb.execute(queryF, parametersF);

		while (resultF.hasNext()) {
			Map<String, Object> row = resultF.next();

			List<Node> ports = (List<Node>) row.get("ports");
			List<Node> childGlyphs = (List<Node>) row.get("childGlyph");

			Glyph glyph;
			String glyphId = (String) ((Node) row.get("g")).getProperty("id");

			if (glist.containsKey(glyphId))
				glyph = glist.get(glyphId);
			else {
				glyph = new Glyph();
			}

			glyph.setId(glyphId);

			glyph.setClazz(((Node) row.get("g")).getLabels().iterator().next().toString().replaceAll("_", " "));

			if (!((String) ((Node) row.get("g")).getProperty("compRef")).equals("notexist")) {
				Glyph glyphforCompartment = new Glyph();
				comprefList.add((String) ((Node) row.get("g")).getProperty("compRef"));

				glyphforCompartment.setId((String) ((Node) row.get("g")).getProperty("compRef"));
				// get compref here
				glyph.setCompartmentRef(glyphforCompartment);
			}
			Bbox bBox = new Bbox();
			float x = (float) ((Node) row.get("g")).getProperty("x");
			float y = (float) ((Node) row.get("g")).getProperty("y");
			float w = (float) ((Node) row.get("g")).getProperty("w");
			float h = (float) ((Node) row.get("g")).getProperty("h");
			bBox.setH((float) h);
			bBox.setW((float) w);
			bBox.setX((float) x);
			bBox.setY((float) y);

			glyph.setBbox(bBox);

			org.sbgn.bindings.Label label = new org.sbgn.bindings.Label();
			String lbl = (String) ((Node) row.get("g")).getProperty("label");
			label.setText(lbl);
			glyph.setLabel(label);

			State state = new State();
			String st = (String) ((Node) row.get("g")).getProperty("stateVal");
			state.setValue(st);
			glyph.setState(state);

			// ports
			ports.forEach(item -> {
				Port port = new Port();
				float xPort = (float) item.getProperty("x");
				float yPort = (float) item.getProperty("y");
				String Pid = (String) item.getProperty("id");

				port.setId(Pid);
				port.setX((int) xPort);
				port.setY((int) yPort);

				glyph.getPort().add(port);
				portlist.put(Pid, port);

			});

			childGlyphs.forEach(item -> {

				Glyph childglyph;
				String childglyphId = (String) item.getProperty("id");

				if (glist.containsKey(childglyphId))
					childglyph = glist.get(childglyphId);
				else {
					childglyph = new Glyph();				
				}
				childglyph.setId(childglyphId);

				childglyph.setClazz(item.getLabels().iterator().next().toString().replaceAll("_", " "));
				Bbox childbBox = new Bbox();
				float cx = (float) item.getProperty("x");
				float cy = (float) item.getProperty("y");
				float cw = (float) item.getProperty("w");
				float ch = (float) item.getProperty("h");

				childbBox.setH((float) ch);
				childbBox.setW((float) cw);
				childbBox.setX((float) cx);
				childbBox.setY((float) cy);

				childglyph.setBbox(childbBox);

				org.sbgn.bindings.Label childlabel = new org.sbgn.bindings.Label();
				String clbl = (String) item.getProperty("label");
				childlabel.setText(clbl);
				childglyph.setLabel(childlabel);

				State cstate = new State();
				String cst = (String) item.getProperty("stateVal");
				cstate.setValue(cst);
				childglyph.setState(cstate);

				if (!((String) item.getProperty("compRef")).equals("notexist")) {
					comprefList.add((String) item.getProperty("compRef"));
					Glyph cglyphforCompartment = new Glyph();
					cglyphforCompartment.setId((String) item.getProperty("compRef"));
					childglyph.setCompartmentRef(cglyphforCompartment);

				}

				glist.putIfAbsent(childglyph.getId(), childglyph);
				set.add(childglyph.getId());
				glyph.getGlyph().add(childglyph);

			});

			glist.putIfAbsent(glyph.getId(), glyph);

			List<Node> siblings = (List<Node>) row.get("sblgList");
			List<Node> parentList = (List<Node>) row.get("prntList");

			if (parentList.size() > 0) {
				Node parent = parentList.get(0);
				Glyph glyphParent;
				String glyphParentId = (String) (parent).getProperty("id");

				if (glist.containsKey(glyphParentId))
					glyphParent = glist.get(glyphParentId);
				else {
					glyphParent = new Glyph();
					glyphParent.setId(glyphParentId);

					glyphParent.setClazz(parent.getLabels().iterator().next().toString().replaceAll("_", " "));

					if (!((String) parent.getProperty("compRef")).equals("notexist")) {
						Glyph glyphforCompartmentp = new Glyph();
						comprefList.add((String) parent.getProperty("compRef"));
						glyphforCompartmentp.setId((String) parent.getProperty("compRef"));
						glyph.setCompartmentRef(glyphforCompartmentp);
					}
					Bbox bBoxp = new Bbox();
					float xxx = (float) parent.getProperty("x");
					float yyy = (float) parent.getProperty("y");
					float www = (float) parent.getProperty("w");
					float hhh = (float) parent.getProperty("h");
					bBoxp.setH((float) hhh);
					bBoxp.setW((float) www);
					bBoxp.setX((float) xxx);
					bBoxp.setY((float) yyy);

					glyphParent.setBbox(bBoxp);

					org.sbgn.bindings.Label labelp = new org.sbgn.bindings.Label();
					String lblp = (String) parent.getProperty("label");
					labelp.setText(lblp);
					glyphParent.setLabel(labelp);

					State statep = new State();
					String stp = (String) parent.getProperty("stateVal");
					statep.setValue(stp);
					glyphParent.setState(statep);

					glyphParent.getGlyph().add(glyph);

					siblings.forEach(item -> {

						Glyph childglyph;
						String childglyphId = (String) item.getProperty("id");

						if (glist.containsKey(childglyphId))
							childglyph = glist.get(childglyphId);
						else {
							childglyph = new Glyph();
						}
						childglyph.setId(childglyphId);

						childglyph.setClazz(item.getLabels().iterator().next().toString().replaceAll("_", " "));
						Bbox childbBox = new Bbox();
						float cx = (float) item.getProperty("x");
						float cy = (float) item.getProperty("y");
						float cw = (float) item.getProperty("w");
						float ch = (float) item.getProperty("h");

						childbBox.setH((float) ch);
						childbBox.setW((float) cw);
						childbBox.setX((float) cx);
						childbBox.setY((float) cy);

						childglyph.setBbox(childbBox);

						org.sbgn.bindings.Label childlabel = new org.sbgn.bindings.Label();
						String clbl = (String) item.getProperty("label");
						childlabel.setText(clbl);
						childglyph.setLabel(childlabel);

						State cstate = new State();
						String cst = (String) item.getProperty("stateVal");
						cstate.setValue(cst);
						childglyph.setState(cstate);

						if (!((String) item.getProperty("compRef")).equals("notexist")) {
							comprefList.add((String) item.getProperty("compRef"));
							Glyph cglyphforCompartment = new Glyph();
							cglyphforCompartment.setId((String) item.getProperty("compRef"));
							childglyph.setCompartmentRef(cglyphforCompartment);

						}

						glist.putIfAbsent(childglyph.getId(), childglyph);
						set.add(childglyph.getId());
						glyphParent.getGlyph().add(childglyph);

					});
				}
				glist.putIfAbsent(glyphParent.getId(), glyphParent);
			}

		}

		// get compref here
		String queryComprefs = "Match (g)  where g.id in {comprefIds} return g";

		Map<String, Object> parametersComprefs = new HashMap<String, Object>();
		parametersComprefs.put("comprefIds", comprefList.toArray());
		Result resultCompRef = graphDb.execute(queryComprefs, parametersComprefs);

		while (resultCompRef.hasNext()) {
			Map<String, Object> row = resultCompRef.next();

			Glyph glyph;
			String glyphId = (String) ((Node) row.get("g")).getProperty("id");
			if (glist.containsKey(glyphId))
				glyph = glist.get(glyphId);
			else {
				glyph = new Glyph();
			}

			glyph.setId(glyphId);

			glyph.setClazz(((Node) row.get("g")).getLabels().iterator().next().toString().replaceAll("_", " "));

			if (!((String) ((Node) row.get("g")).getProperty("compRef")).equals("notexist")) {
				Glyph glyphforCompartment = new Glyph();
				comprefList.add((String) ((Node) row.get("g")).getProperty("compRef"));
				glyphforCompartment.setId((String) ((Node) row.get("g")).getProperty("compRef"));
				glyph.setCompartmentRef(glyphforCompartment);
			}
			Bbox bBox = new Bbox();
			float x = (float) ((Node) row.get("g")).getProperty("x");
			float y = (float) ((Node) row.get("g")).getProperty("y");
			float w = (float) ((Node) row.get("g")).getProperty("w");
			float h = (float) ((Node) row.get("g")).getProperty("h");
			bBox.setH((float) h);
			bBox.setW((float) w);
			bBox.setX((float) x);
			bBox.setY((float) y);

			glyph.setBbox(bBox);

			org.sbgn.bindings.Label label = new org.sbgn.bindings.Label();
			String lbl = (String) ((Node) row.get("g")).getProperty("label");
			label.setText(lbl);
			glyph.setLabel(label);

			State state = new State();
			String st = (String) ((Node) row.get("g")).getProperty("stateVal");
			state.setValue(st);
			glyph.setState(state);
			// ports

			glist.putIfAbsent(glyph.getId(), glyph);
		}
	}



	private void retrieveRelationsForResultSBGNMap(GraphDatabaseService graphDb, Collection<Relationship> collection,
			HashMap<String, Glyph> glist, HashMap<String, Port> portlist, org.sbgn.bindings.Map sbgnMap) {
		String query = "MATCH (a)-[r]->(b) where r.startX is not null  and r in {rels} return a.id as sid,b.id as tid, r.startX as xx,r.startY as yy, r.endX as ex, r.endY as ey, r.aid as id, type(r) as class";

		Map<String, Object> parameters1 = new HashMap<String, Object>();

		parameters1.put("rels", collection);

		Result result2 = graphDb.execute(query, parameters1);

		while (result2.hasNext()) {
			Map<String, Object> row2 = result2.next();

			float startX = (float) row2.get("xx");
			float startY = (float) row2.get("yy");
			Start start = new Start();
			start.setX((float) startX);
			start.setY((float) startY);
			float endX = (float) row2.get("ex");
			float endY = (float) row2.get("ey");
			String aid = (String) row2.get("id");
			String clazz = (String) row2.get("class");
			clazz = clazz.replaceAll("_", " ");
			End end = new End();
			end.setX((float) endX);
			end.setY((float) endY);
			Arc arc = new Arc();
			arc.setClazz(clazz);
			arc.setId(aid);
			arc.setEnd(end);
			arc.setStart(start);
			if (glist.get(row2.get("sid")) != null) {
				arc.setSource(glist.get(row2.get("sid")));
			} else {
				arc.setSource(portlist.get(row2.get("sid")));
			}

			if (glist.get(row2.get("tid")) != null) {
				arc.setTarget(glist.get(row2.get("tid")));
			} else {
				arc.setTarget(portlist.get(row2.get("tid")));
			}

			sbgnMap.getArc().add(arc);
		}
	}

	private String ifGlyphIsDissociation(double direction) {
		String query3 = "";
		if (direction == 1) {
			query3 = "match (asc:dissociation) where asc.id = {id}  optional match p1= (asc)--(:Port)-[]->(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

		} else if (direction == 2) {
			query3 = "match (asc:dissociation) where asc.id = {id}  optional match p1= (asc)--(:Port)<-[]-(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

		} else if (direction == 0) {
			query3 = "match (asc:dissociation) where asc.id = {id}  optional match p1= (asc)--(:Port)--(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

		}
		return query3;
	}

	private String ifGlyphIsAssociation(double direction) {
		String query3 = "";
		if (direction == 1) {
			query3 = "match (asc:association) where asc.id = {id}  optional match p1= (asc)--(:Port)-[]->(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

		} else if (direction == 2) {
			query3 = "match (asc:association) where asc.id = {id}  optional match p1= (asc)--(:Port)<-[]-(a) optional match p2=(a)-[:resideIn*]-(b)  return [a.id, b.id] as idList, [p1,p2] as pathList";

		} else if (direction == 0) {
			query3 = "match (asc:association) where asc.id = {id}  optional match p1= (asc)--(:Port)--(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

		}
		return query3;
	}

	private String ifGlyphIsUncertainProcess(double direction) {
		String query3 = "";
		if (direction == 1) {
			query3 = "match (p:uncertain_process) where p.id = {id} optional match p1= (p)--(:Port)-[]->(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)-[]->(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
		} else if (direction == 2) {
			query3 = "match (p:uncertain_process) where p.id = {id} optional match p1= (p)--(:Port)<-[]-(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)<-[]-(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
		} else if (direction == 0) {
			query3 = "match (p:uncertain_process) where p.id = {id} optional match p1= (p)--(:Port)--(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)--(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
		}
		return query3;
	}

	private String ifGlyphIsOmittedProcess(double direction) {
		String query3 = "";
		if (direction == 1) {
			query3 = "match (p:omitted_process) where p.id = {id} optional match p1= (p)--(:Port)-[]->(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)-[]->(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id]as idList,  [p1,p2,p3,p4] as pathList";
		} else if (direction == 2) {
			query3 = "match (p:omitted_process) where p.id = {id} optional match p1= (p)--(:Port)<-[]-(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)<-[]-(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
		} else if (direction == 0) {
			query3 = "match (p:omitted_process) where p.id = {id} optional match p1= (p)--(:Port)--(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)--(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
		}
		return query3;
	}

	private String ifGlyphIsProcess(double direction) {
		String query3 = "";
		if (direction == 1) {
			query3 = "match (p:process) where p.id = {id} optional match p1= (p)--(:Port)-[]->(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)-[]->(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
		} else if (direction == 2) {
			query3 = "match (p:process) where p.id = {id} optional match p1= (p)--(:Port)<-[]-(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)<-[]-(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
		} else if (direction == 0) {
			query3 = "match (p:process) where p.id = {id} optional match p1= (p)--(:Port)--(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)--(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
		}
		return query3;
	}

	private String ifGlyphIsPort(double direction) {
		String query3 = "";
		if (direction == 0) {
			query3 = "match (po:Port) where po.id = {id}  optional match p1= (po)--(pr)--(:Port)--(a) where  pr:dissociation or  pr:process or pr:association or pr:omitted_process or pr:uncertain_process optional match p4=(a)-[:resideIn*]-(m) optional match p2 = (po)--(:process)--(b) where not b:Port optional match p7= (po)--(:omitted_process)--(f) where not f:Port optional match p8= (po)--(:uncertain_process)--(g) where not g:Port optional match p5=(b)-[:resideIn*]-(n) optional match p3 = (po)--(c) where not c:process and not c:omitted_process and not c:uncertain_process and not c:association  and not c:dissociation optional match p6=(c)-[:resideIn*]-(k) return [a.id,b.id, c.id, f.id, g.id, m.id, n.id, k.id] as idList, [p1,p2,p3,p4,p5,p6,p7,p8] as pathList";

		} else if (direction == 1) {
			query3 = "match (po:Port) where po.id = {id}  optional match p1= (po)--(pr)--(:Port)-[]->(a) where  pr:dissociation or  pr:process or pr:association or pr:omitted_process or pr:uncertain_process optional match p4=(a)-[:resideIn*]-(m) optional match p2 = (po)--(:process)-[]->(b) where not b:Port optional match p7= (po)--(:omitted_process)-[]->(f) where not f:Port optional match p8= (po)--(:uncertain_process)-[]->(g) where not g:Port optional match p5=(b)-[:resideIn*]-(n) optional match p3 = (po)-[]->(c) where not c:process and not c:omitted_process and not c:uncertain_process and not c:association  and not c:dissociation optional match p6=(c)-[:resideIn*]-(k) return [a.id,b.id, c.id, f.id, g.id, m.id, n.id, k.id] as idList, [p1,p2,p3,p4,p5,p6,p7,p8] as pathList";

		} else if (direction == 2) {
			query3 = "match (po:Port) where po.id = {id}  optional match p1= (po)--(pr)--(:Port)<-[]-(a) where  pr:dissociation or  pr:process or pr:association or pr:omitted_process or pr:uncertain_process optional match p4=(a)-[:resideIn*]-(m) optional match p2 = (po)--(:process)<-[]-(b) where not b:Port optional match p7= (po)--(:omitted_process)<-[]-(f) where not f:Port optional match p8= (po)--(:uncertain_process)<-[]-(g) where not g:Port optional match p5=(b)-[:resideIn*]-(n) optional match p3 = (po)<-[]-(c) where not c:process and not c:omitted_process and not c:uncertain_process and not c:association  and not c:dissociation optional match p6=(c)-[:resideIn*]-(k) return [a.id,b.id, c.id, f.id, g.id, m.id, n.id, k.id] as idList, [p1,p2,p3,p4,p5,p6,p7,p8] as pathList";

		}
		return query3;
	}

	private void addPathItemToPathListContainerOfRoot(double count, PathsBetweenRoot root) {
		
		for(FreshIdItem freshid : 	root.FreshIdMap.values()){
				double compkey = count +1;
				if(freshid !=null){					
					List<PathListITem> pitemList = root.PathListContainer.stream().filter( p -> p.key.equals( freshid.pid.toString()+":"+count)).collect(Collectors.toList());
			
							if(pitemList != null && !freshid.pid.equals("") ){							
								for (PathListITem pitem : pitemList){										
									if(pitem.endNodeDepth +1 == count +1 && pitem.pathNodeIdList.size()== count +1 ){
										PathListITem fitem  = new PathListITem(freshid.sid.toString()+":"+compkey,freshid.sid.toString(),compkey);											
									fitem.pathNodeIdList.addAll(pitem.pathNodeIdList);
									fitem.pathNodeIdList.add(freshid.sid.toString());
									root.PathListContainer.add(fitem);
								}
							}
						}
				}
		}
	}
	
	private void AddToFreshPidTupleList(HashMap<String, FreshIdItem> freshPidTupleList, Collection<Object> list,
			Object pid) {

		list.forEach(iditem -> {
			if (iditem != null && iditem != pid) {
				String k =  pid + iditem.toString();
				freshPidTupleList.put(k, new FreshIdItem(iditem, pid));
			}

		});	

	}

	private void GoiNodePathExtract(GOINodeContainer gOINodeContainer, List<String> list, List<Path> list2,
			double depth) {
		list.forEach(FreshIdListItem -> {
			gOINodeContainer.listOfpathslist.putIfAbsent((String) FreshIdListItem, list2);
			
		});

	}
	
private void traverse1(GraphDatabaseService graphDb,Node blistitem,  Set<Path> pathList  ) {
		
		if(blistitem != null){

			double direction =0;
			
			for(org.neo4j.graphdb.Label blistitemlabel : blistitem.getLabels()) {
				
				if (blistitemlabel.name().equals("Port")) {

					String query3 = ifGlyphIsPort(direction);					
					Map<String, Object> parameters3 = new HashMap<String, Object>();				
					parameters3.put("id", blistitem.getProperty("id"));
					Result result3 = graphDb.execute(query3, parameters3);			
					while (result3.hasNext()) {
						Map<String, Object> row3 = result3.next();		
						pathList.addAll((List<Path>) row3.get("pathList"));
					}
				
				} else if (blistitemlabel.name().equals("process")) {

					String query3 = ifGlyphIsProcess(direction);

					Map<String, Object> parameters3 = new HashMap<String, Object>();					
					parameters3.put("id", blistitem.getProperty("id"));
					Result result3 = graphDb.execute(query3, parameters3);
					while (result3.hasNext()) {
						Map<String, Object> row3 = result3.next();					;
						pathList.addAll((List<Path>) row3.get("pathList"));					

					}
				} else if (blistitemlabel.name().equals("omitted_process")) {

				
					String query3 = ifGlyphIsOmittedProcess(direction);

					Map<String, Object> parameters3 = new HashMap<String, Object>();
			
					parameters3.put("id", blistitem.getProperty("id"));

					Result result3 = graphDb.execute(query3, parameters3);
					
					while (result3.hasNext()) {
						Map<String, Object> row3 = result3.next();
						pathList.addAll((List<Path>) row3.get("pathList"));
					}

				} else if (blistitemlabel.name().equals("uncertain_process")) {
					
					String query3 = ifGlyphIsUncertainProcess(direction);

					Map<String, Object> parameters3 = new HashMap<String, Object>();

					parameters3.put("id", blistitem.getProperty("id"));

					Result result3 = graphDb.execute(query3, parameters3);
				
					while (result3.hasNext()) {
						Map<String, Object> row3 = result3.next();						
						pathList.addAll((List<Path>) row3.get("pathList"));	
					}

				} else if (blistitemlabel.name().equals("association")) {											

					String query3 = ifGlyphIsAssociation(direction);

					Map<String, Object> parameters3 = new HashMap<String, Object>();
				
					parameters3.put("id", blistitem.getProperty("id"));

					Result result3 = graphDb.execute(query3, parameters3);
					
					while (result3.hasNext()) {
						Map<String, Object> row3 = result3.next();					
						pathList.addAll((List<Path>) row3.get("pathList"));
					}

				} else if (blistitemlabel.name().equals("dissociation")) {

					String query3 = ifGlyphIsDissociation(direction);
					Map<String, Object> parameters3 = new HashMap<String, Object>();
					parameters3.put("id", blistitem.getProperty("id"));
					Result result3 = graphDb.execute(query3, parameters3);
					while (result3.hasNext()) {
						Map<String, Object> row3 = result3.next();
						pathList.addAll((List<Path>) row3.get("pathList"));
					}

				}

				else {
					;
				}		
				
			}	
		}
	}

	
	private void Traverse1AndExtractRelationsAndNodeForProcesses(GraphDatabaseService graphDb,
			Set<Node> nodesListPurify, Set<Relationship> relsListPurify) {
		Set<Path> pathListforPostProcess = new HashSet<Path>();
		for( Node item : nodesListPurify){
			
			traverse1(graphDb, item, pathListforPostProcess);
			
		}
		
		pathListforPostProcess.forEach(pathitem -> {
			  
			  if (pathitem!= null && pathitem.relationships() != null) {
			 
			  pathitem.relationships().forEach(relitem -> {  
				if(relitem != null  && ! relsListPurify.stream().filter(r-> r.getId() == relitem.getId()).findAny().isPresent())
				{
					relsListPurify.add(relitem);
					
				}
			  
			  });
			 
			 } 
			  
			  if (pathitem!= null && pathitem.nodes() != null) {
					 
				  pathitem.nodes().forEach(nodeitem -> {
					  if (nodeitem != null && !nodesListPurify.stream().filter(n-> n.getId() == nodeitem.getId()).findAny().isPresent() )
					  { 						
						  nodesListPurify.add( nodeitem); 								  
					  }
				  });						 
				 } 					  
		}				
			 
			 );
	}

}
