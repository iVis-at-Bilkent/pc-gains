package PC_Gains.Procedures;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.builtinprocs.SchemaProcedure.GraphResult;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.Sbgn;
import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.sbgn.*;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Arc.End;
import org.sbgn.bindings.Arc.Start;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Glyph.State;
import org.sbgn.bindings.Label;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.Sbgn;
/**
 * Hello world!
 *
 */
public class App 
{

	 class GOINodeObject {	
			
			GOINodeObject( String id , double depth, List<Path>  pathList ){
				this.nodeId= id;
				this.dist = depth;
				this.pathList = pathList;		
								
			}
			String nodeId;
			double dist = 999;
			 List<Path>  pathList ;
		}
		

		 class GOINodeContainer {				 
			HashMap<String, GOINodeObject> GOINodeObjectList = new HashMap<String, GOINodeObject>();
			
			HashMap<String, Node> nodesList = new HashMap<String, Node>();
			HashMap<String, Relationship> relsList = new HashMap<String, Relationship>();
			HashMap<String, List<Path>> listOfpathslist = new HashMap<String, List<Path>>();
			Set<Path> pathList = new HashSet<Path>();
			Set<String> finalIds = new HashSet<String>();
			String rootId;
			Path apath  ;	
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
			HashMap<String, PathListITem> PathListContainer = new HashMap<String, PathListITem>();	
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
			
			PathListITem( String endNodeId, double d){
				this.endNodeDepth = d;
				this.endNodeId = endNodeId;		
			}
			String endNodeId;
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
			Path apath  ;		
		}
		
		@Context
		public GraphDatabaseService db;

		@Procedure(name = "org.sbn.example.proc.InsertGraph", mode = Mode.WRITE)
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

		

			return Stream.of(new Output(writeToString1(sbgn)));
		}

		@Procedure(name = "org.sbn.example.proc.ReadGraphFromDb", mode = Mode.READ)
		public Stream<Output> ReadGraphFromDb() throws JAXBException {

			return Stream.of(new Output(ReadGraph()));
		}

		@Procedure(name = "org.sbn.example.proc.Neighbors", mode = Mode.WRITE)
		public Stream<Output> Neighbors(@Name("genesList") String geneList, @Name("limit") double limit)
				throws JAXBException {
			if (geneList == null) {
				return null;
			}

			return Stream.of(new Output(neighborsBFS(geneList, limit)));
		}

		@Procedure(name = "org.sbn.example.proc.StreamHighlight", mode = Mode.WRITE)
		public Stream<Output> StreamHighlight(@Name("genesList") String geneList, @Name("limit") double limit, @Name("dir") double dir)
				throws JAXBException {
			if (geneList == null) {
				return null;
			}

			return Stream.of(new Output(stream(geneList, limit,dir)));
		}		
		
		@Procedure(name = "org.sbn.example.proc.StreamPaths", mode = Mode.WRITE)
		public Stream<Output> StreamPaths(@Name("genesList") String geneList, @Name("limit") double limit, @Name("dir") double dir)
				throws JAXBException {
			if (geneList == null) {
				return null;
			}

			return Stream.of(new Output(stream2(geneList, limit,dir)));
		}
		
		@Procedure(name = "org.sbn.example.proc.PathsBetween", mode = Mode.WRITE)
		public Stream<Output> PathsBetween(@Name("genesList") String genesList, @Name("genesListTarget") String genesListTarget,@Name("limit") double limit, @Name("addition") double addition)
				throws JAXBException {
			if (genesList == null) {
				return null;
			}
			if (genesListTarget == null) {
				return null;
			}

			return Stream.of(new Output(PathsBetweenFunc( genesList, genesListTarget, limit,  addition)));
		}
		
		@Procedure(name = "org.sbn.example.proc.GOI", mode = Mode.WRITE)
		public Stream<Output> GOI(@Name("genesList") String genesList,@Name("limit") double limit, @Name("direction") double direction)
				throws JAXBException {
			if (genesList == null) {
				return null;
			}		

			return Stream.of(new Output(GoIfunc( genesList, limit,  direction)));
		}
			
		private String stream(String genesList, double limita, double direction) throws JAXBException {
		

			ArrayList<StreamObject> streamObjList = new ArrayList<StreamObject>();
			Glyph a;
			HashMap<String, Glyph> glist = new HashMap<String, Glyph>();
			HashMap<String, Port> portlist = new HashMap<String, Port>();
			org.sbgn.bindings.Map sbgnMap = new org.sbgn.bindings.Map();
			Set<String> set = new HashSet<>();
			Set<String> comprefList = new HashSet<String>();

			

				Set<Node> nodeList = new HashSet<Node>();
				 
				String queryM = "match (a) where a.label in {lists}  return  collect(a.id) as idlist";


				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("lists", genesList.split(" "));
			

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
						//System.out.println("buss");

						FreshIdListToIterate.removeAll(OldIdListToIterate);
						String query = "";

						if (direction == 0) {
							query = "match p= (a)--(b) where a.id in {lists} optional match p2 = (b)-[:resideIn*]-(c) return [b, c] as blist, [p , p2] as p2List";
						} else if (direction == 1) {
							query = "match p= (a)-[]->(b) where a.id in {lists} optional match p2 = (b)-[:resideIn*]-(c) return [b, c] as blist, [p , p2] as p2List";
						} else if (direction == 2) {
							query = "match p= (a)<-[]-(b) where a.id in {lists} optional match p2 = (b)-[:resideIn*]-(c) return [b, c] as blist, [p , p2] as p2List";
						}

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
								
								if(blistitem != null){


									blistitem.getLabels().forEach(blistitemlabel -> {
										
										if (blistitemlabel.name().equals("Port")) {

											String query3 = "";
											if (direction == 0) {
												query3 = "match (po:Port) where po.id = {id}  optional match p1= (po)--(pr)--(:Port)--(a) where  pr:dissociation or  pr:process or pr:association or pr:omitted_process or pr:uncertain_process optional match p4=(a)-[:resideIn*]-(m) optional match p2 = (po)--(:process)--(b) where not b:port optional match p7= (po)--(:omitted_process)--(f) where not f:port optional match p8= (po)--(:uncertain_process)--(g) where not g:port optional match p5=(b)-[:resideIn*]-(n) optional match p3 = (po)--(c) where not c:process and not c:omitted_process and not c:uncertain_process and not c:association  and not c:dissociation optional match p6=(c)-[:resideIn*]-(k) return [a.id,b.id, c.id, f.id, g.id, m.id, n.id, k.id] as idList, [p1,p2,p3,p4,p5,p6,p7,p8] as pathList";

											} else if (direction == 1) {
												query3 = "match (po:Port) where po.id = {id}  optional match p1= (po)--(pr)--(:Port)-[]->(a) where  pr:dissociation or  pr:process or pr:association or pr:omitted_process or pr:uncertain_process optional match p4=(a)-[:resideIn*]-(m) optional match p2 = (po)--(:process)-[]->(b) where not b:port optional match p7= (po)--(:omitted_process)-[]->(f) where not f:port optional match p8= (po)--(:uncertain_process)-[]->(g) where not g:port optional match p5=(b)-[:resideIn*]-(n) optional match p3 = (po)-[]->(c) where not c:process and not c:omitted_process and not c:uncertain_process and not c:association  and not c:dissociation optional match p6=(c)-[:resideIn*]-(k) return [a.id,b.id, c.id, f.id, g.id, m.id, n.id, k.id] as idList, [p1,p2,p3,p4,p5,p6,p7,p8] as pathList";

											} else if (direction == 2) {
												query3 = "match (po:Port) where po.id = {id}  optional match p1= (po)--(pr)--(:Port)<-[]-(a) where  pr:dissociation or  pr:process or pr:association or pr:omitted_process or pr:uncertain_process optional match p4=(a)-[:resideIn*]-(m) optional match p2 = (po)--(:process)<-[]-(b) where not b:port optional match p7= (po)--(:omitted_process)<-[]-(f) where not f:port optional match p8= (po)--(:uncertain_process)<-[]-(g) where not g:port optional match p5=(b)-[:resideIn*]-(n) optional match p3 = (po)<-[]-(c) where not c:process and not c:omitted_process and not c:uncertain_process and not c:association  and not c:dissociation optional match p6=(c)-[:resideIn*]-(k) return [a.id,b.id, c.id, f.id, g.id, m.id, n.id, k.id] as idList, [p1,p2,p3,p4,p5,p6,p7,p8] as pathList";

											}
											
											Map<String, Object> parameters3 = new HashMap<String, Object>();
											
											parameters3.put("id", blistitem.getProperty("id"));

											Result result3 = db.execute(query3, parameters3);
											
											while (result3.hasNext()) {
												Map<String, Object> row3 = result3.next();

												FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
												
												strObj.pathList.addAll((List<Path>) row3.get("pathList"));											

											}

										
										} else if (blistitemlabel.name().equals("process")) {
											
											String query3 = "";
											if (direction == 1) {
												query3 = "match (p:process) where p.id = {id} optional match p1= (p)--(:Port)-[]->(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)-[]->(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
											} else if (direction == 2) {
												query3 = "match (p:process) where p.id = {id} optional match p1= (p)--(:Port)<-[]-(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)<-[]-(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
											} else if (direction == 0) {
												query3 = "match (p:process) where p.id = {id} optional match p1= (p)--(:Port)--(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)--(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
											}

											Map<String, Object> parameters3 = new HashMap<String, Object>();
											
											parameters3.put("id", blistitem.getProperty("id"));

											Result result3 = db.execute(query3, parameters3);
											
											while (result3.hasNext()) {
												Map<String, Object> row3 = result3.next();											

												FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
												
												strObj.pathList.addAll((List<Path>) row3.get("pathList"));

											}
										} else if (blistitemlabel.name().equals("omitted_process")) {
											
											String query3 = "";
											if (direction == 1) {
												query3 = "match (p:omitted_process) where p.id = {id} optional match p1= (p)--(:Port)-[]->(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)-[]->(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id]as idList,  [p1,p2,p3,p4] as pathList";
											} else if (direction == 2) {
												query3 = "match (p:omitted_process) where p.id = {id} optional match p1= (p)--(:Port)<-[]-(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)<-[]-(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
											} else if (direction == 0) {
												query3 = "match (p:omitted_process) where p.id = {id} optional match p1= (p)--(:Port)--(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)--(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
											}

											Map<String, Object> parameters3 = new HashMap<String, Object>();
											
											parameters3.put("id", blistitem.getProperty("id"));

											Result result3 = db.execute(query3, parameters3);
											
											while (result3.hasNext()) {
												Map<String, Object> row3 = result3.next();
												

												FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
												
												strObj.pathList.addAll((List<Path>) row3.get("pathList"));										

											}

										} else if (blistitemlabel.name().equals("uncertain_process")) {

											
											String query3 = "";
											if (direction == 1) {
												query3 = "match (p:uncertain_process) where p.id = {id} optional match p1= (p)--(:Port)-[]->(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)-[]->(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
											} else if (direction == 2) {
												query3 = "match (p:uncertain_process) where p.id = {id} optional match p1= (p)--(:Port)<-[]-(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)<-[]-(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
											} else if (direction == 0) {
												query3 = "match (p:uncertain_process) where p.id = {id} optional match p1= (p)--(:Port)--(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)--(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
											}

											Map<String, Object> parameters3 = new HashMap<String, Object>();
										
											parameters3.put("id", blistitem.getProperty("id"));

											Result result3 = db.execute(query3, parameters3);
											
											while (result3.hasNext()) {
												Map<String, Object> row3 = result3.next();										

												FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
											
												strObj.pathList.addAll((List<Path>) row3.get("pathList"));											

											}

										} else if (blistitemlabel.name().equals("association")) {

											String query3 = "";
											if (direction == 1) {
												query3 = "match (asc:association) where asc.id = {id}  optional match p1= (asc)--(:Port)-[]->(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

											} else if (direction == 2) {
												query3 = "match (asc:association) where asc.id = {id}  optional match p1= (asc)--(:Port)<-[]-(a) optional match p2=(a)-[:resideIn*]-(b)  return [a.id, b.id] as idList, [p1,p2] as pathList";

											} else if (direction == 0) {
												query3 = "match (asc:association) where asc.id = {id}  optional match p1= (asc)--(:Port)--(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

											}

											Map<String, Object> parameters3 = new HashMap<String, Object>();

											parameters3.put("id", blistitem.getProperty("id"));

											Result result3 = db.execute(query3, parameters3);

											while (result3.hasNext()) {
												Map<String, Object> row3 = result3.next();

												FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
												strObj.pathList.addAll((List<Path>) row3.get("pathList"));
												
											}

										} else if (blistitemlabel.name().equals("dissociation")) {
											
											String query3 = "";
											if (direction == 1) {
												query3 = "match (asc:dissociation) where asc.id = {id}  optional match p1= (asc)--(:Port)-[]->(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

											} else if (direction == 2) {
												query3 = "match (asc:dissociation) where asc.id = {id}  optional match p1= (asc)--(:Port)<-[]-(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

											} else if (direction == 0) {
												query3 = "match (asc:dissociation) where asc.id = {id}  optional match p1= (asc)--(:Port)--(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

											}
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
							pathItem.nodes().forEach(nodeItem -> {
								if (nodeItem != null)
									streamItem.nodesList.putIfAbsent(nodeItem.getProperty("id").toString(), nodeItem);

							});
						}

					});
				});	


			Set<Node> finalNodes = new HashSet<Node>();

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
				strforhighlight = strforhighlight.substring(0, strforhighlight.length() - 1);
			
			return strforhighlight;		
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
					String queryM = "match (a) where a.label in {lists}  return  collect(a.id) as idlist";

					
					Map<String, Object> parameters = new HashMap<String, Object>();
					parameters.put("lists", genesList.split(" "));
					
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
							String query = "";

							if (direction == 0) {
								query = "match p= (a)--(b) where a.id in {lists} optional match p2 = (b)-[:resideIn*]-(c) return [b, c] as blist, [p , p2] as p2List";
							} else if (direction == 1) {
								query = "match p= (a)-[]->(b) where a.id in {lists} optional match p2 = (b)-[:resideIn*]-(c) return [b, c] as blist, [p , p2] as p2List";
							} else if (direction == 2) {
								query = "match p= (a)<-[]-(b) where a.id in {lists} optional match p2 = (b)-[:resideIn*]-(c) return [b, c] as blist, [p , p2] as p2List";
							}

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
									
									if(blistitem != null){

										blistitem.getLabels().forEach(blistitemlabel -> {
											
											if (blistitemlabel.name().equals("Port")) {

												String query3 = "";
												if (direction == 0) {
													query3 = "match (po:Port) where po.id = {id}  optional match p1= (po)--(pr)--(:Port)--(a) where  pr:dissociation or  pr:process or pr:association or pr:omitted_process or pr:uncertain_process optional match p4=(a)-[:resideIn*]-(m) optional match p2 = (po)--(:process)--(b) where not b:port optional match p7= (po)--(:omitted_process)--(f) where not f:port optional match p8= (po)--(:uncertain_process)--(g) where not g:port optional match p5=(b)-[:resideIn*]-(n) optional match p3 = (po)--(c) where not c:process and not c:omitted_process and not c:uncertain_process and not c:association  and not c:dissociation optional match p6=(c)-[:resideIn*]-(k) return [a.id,b.id, c.id, f.id, g.id, m.id, n.id, k.id] as idList, [p1,p2,p3,p4,p5,p6,p7,p8] as pathList";

												} else if (direction == 1) {
													query3 = "match (po:Port) where po.id = {id}  optional match p1= (po)--(pr)--(:Port)-[]->(a) where  pr:dissociation or  pr:process or pr:association or pr:omitted_process or pr:uncertain_process optional match p4=(a)-[:resideIn*]-(m) optional match p2 = (po)--(:process)-[]->(b) where not b:port optional match p7= (po)--(:omitted_process)-[]->(f) where not f:port optional match p8= (po)--(:uncertain_process)-[]->(g) where not g:port optional match p5=(b)-[:resideIn*]-(n) optional match p3 = (po)-[]->(c) where not c:process and not c:omitted_process and not c:uncertain_process and not c:association  and not c:dissociation optional match p6=(c)-[:resideIn*]-(k) return [a.id,b.id, c.id, f.id, g.id, m.id, n.id, k.id] as idList, [p1,p2,p3,p4,p5,p6,p7,p8] as pathList";

												} else if (direction == 2) {
													query3 = "match (po:Port) where po.id = {id}  optional match p1= (po)--(pr)--(:Port)<-[]-(a) where  pr:dissociation or  pr:process or pr:association or pr:omitted_process or pr:uncertain_process optional match p4=(a)-[:resideIn*]-(m) optional match p2 = (po)--(:process)<-[]-(b) where not b:port optional match p7= (po)--(:omitted_process)<-[]-(f) where not f:port optional match p8= (po)--(:uncertain_process)<-[]-(g) where not g:port optional match p5=(b)-[:resideIn*]-(n) optional match p3 = (po)<-[]-(c) where not c:process and not c:omitted_process and not c:uncertain_process and not c:association  and not c:dissociation optional match p6=(c)-[:resideIn*]-(k) return [a.id,b.id, c.id, f.id, g.id, m.id, n.id, k.id] as idList, [p1,p2,p3,p4,p5,p6,p7,p8] as pathList";

												}
												
												Map<String, Object> parameters3 = new HashMap<String, Object>();
												
												parameters3.put("id", blistitem.getProperty("id"));

												Result result3 = db.execute(query3, parameters3);
												
												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();

													FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
													
													strObj.pathList.addAll((List<Path>) row3.get("pathList"));												

												}
												
											} else if (blistitemlabel.name().equals("process")) {
												
												String query3 = "";
												if (direction == 1) {
													query3 = "match (p:process) where p.id = {id} optional match p1= (p)--(:Port)-[]->(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)-[]->(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												} else if (direction == 2) {
													query3 = "match (p:process) where p.id = {id} optional match p1= (p)--(:Port)<-[]-(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)<-[]-(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												} else if (direction == 0) {
													query3 = "match (p:process) where p.id = {id} optional match p1= (p)--(:Port)--(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)--(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												}

												Map<String, Object> parameters3 = new HashMap<String, Object>();

												parameters3.put("id", blistitem.getProperty("id"));

												Result result3 = db.execute(query3, parameters3);

												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();
													
													FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
													
													strObj.pathList.addAll((List<Path>) row3.get("pathList"));

												}
											} else if (blistitemlabel.name().equals("omitted_process")) {
												
												String query3 = "";
												if (direction == 1) {
													query3 = "match (p:omitted_process) where p.id = {id} optional match p1= (p)--(:Port)-[]->(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)-[]->(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id]as idList,  [p1,p2,p3,p4] as pathList";
												} else if (direction == 2) {
													query3 = "match (p:omitted_process) where p.id = {id} optional match p1= (p)--(:Port)<-[]-(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)<-[]-(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												} else if (direction == 0) {
													query3 = "match (p:omitted_process) where p.id = {id} optional match p1= (p)--(:Port)--(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)--(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												}

												Map<String, Object> parameters3 = new HashMap<String, Object>();
												
												parameters3.put("id", blistitem.getProperty("id"));

												Result result3 = db.execute(query3, parameters3);

												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();
												
													FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));												
													strObj.pathList.addAll((List<Path>) row3.get("pathList"));

												}

											} else if (blistitemlabel.name().equals("uncertain_process")) {
										
												String query3 = "";
												if (direction == 1) {
													query3 = "match (p:uncertain_process) where p.id = {id} optional match p1= (p)--(:Port)-[]->(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)-[]->(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												} else if (direction == 2) {
													query3 = "match (p:uncertain_process) where p.id = {id} optional match p1= (p)--(:Port)<-[]-(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)<-[]-(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												} else if (direction == 0) {
													query3 = "match (p:uncertain_process) where p.id = {id} optional match p1= (p)--(:Port)--(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)--(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												}

												Map<String, Object> parameters3 = new HashMap<String, Object>();
												
												parameters3.put("id", blistitem.getProperty("id"));

												Result result3 = db.execute(query3, parameters3);
											
												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();												
													FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));											
													strObj.pathList.addAll((List<Path>) row3.get("pathList"));
												}

											} else if (blistitemlabel.name().equals("association")) {											

												String query3 = "";
												if (direction == 1) {
													query3 = "match (asc:association) where asc.id = {id}  optional match p1= (asc)--(:Port)-[]->(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

												} else if (direction == 2) {
													query3 = "match (asc:association) where asc.id = {id}  optional match p1= (asc)--(:Port)<-[]-(a) optional match p2=(a)-[:resideIn*]-(b)  return [a.id, b.id] as idList, [p1,p2] as pathList";

												} else if (direction == 0) {
													query3 = "match (asc:association) where asc.id = {id}  optional match p1= (asc)--(:Port)--(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

												}

												Map<String, Object> parameters3 = new HashMap<String, Object>();

												parameters3.put("id", blistitem.getProperty("id"));

												Result result3 = db.execute(query3, parameters3);

												
												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();

													FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
													strObj.pathList.addAll((List<Path>) row3.get("pathList"));	
												}

											} else if (blistitemlabel.name().equals("dissociation")) {
											
												String query3 = "";
												if (direction == 1) {
													query3 = "match (asc:dissociation) where asc.id = {id}  optional match p1= (asc)--(:Port)-[]->(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

												} else if (direction == 2) {
													query3 = "match (asc:dissociation) where asc.id = {id}  optional match p1= (asc)--(:Port)<-[]-(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

												} else if (direction == 0) {
													query3 = "match (asc:dissociation) where asc.id = {id}  optional match p1= (asc)--(:Port)--(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

												}
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

				finalNodes.addAll(streamObjList.get(0).nodesList.values());

				streamObjList.forEach(streamItem -> {

					finalNodes.retainAll(streamItem.nodesList.values());
					
				});		
				
				
				String queryToPurify = "match (a) where a.id in {SourceidList} with a  optional match (b)  where  b in {finalNodes} with a, b optional match  p= (a)-[r*]-(b) where  all(rv IN  relationships(p)  WHERE rv in {relas})  return relationships(p) as rels, nodes(p) as nodes" ;
				Map<String, Object> parametersPurify = new HashMap<String, Object>();
				parametersPurify.put("SourceidList", idList);
				parametersPurify.put("finalNodes", finalNodes);
				parametersPurify.put("relas", allRelations);
				Result resultPurify = db.execute(queryToPurify, parametersPurify);

				while (resultPurify.hasNext()) {
					Map<String, Object> rowPurify = resultPurify.next();				
					if((List<Node>) rowPurify.get("nodes") != null)
					nodesListPurify.addAll((List<Node>) rowPurify.get("nodes"));
					
					if((List<Relationship>) rowPurify.get("rels") !=null)
					relsListPurify.addAll((List<Relationship>) rowPurify.get("rels"));				

				}

		
				
				String queryF = "Match (g)  where  not g:Port and g in {nodes} optional match (g)<-[:resideIn]-(gc) optional match (g)-[:hasPort]->(p:Port) optional match (g)-[:resideIn]->(prnt) optional match (sblg) - [:resideIn] -> (prnt) where sblg <> g return g, collect(gc) as childGlyph, collect(p) as ports, collect(distinct prnt) as prntList, collect(sblg) as sblgList, count(prnt) as  prntcount  order by prntcount desc";

				Map<String, Object> parametersF = new HashMap<String, Object>();
				parametersF.put("nodes", nodesListPurify);
				
				Result resultF = db.execute(queryF, parametersF);

				while (resultF.hasNext()) {
					Map<String, Object> row = resultF.next();

					List<Node> ports = (List<Node>) row.get("ports");
					List<Node> childGlyphs = (List<Node>) row.get("childGlyph");

					Glyph glyph;
					String glyphId = (String) ((Node) row.get("g")).getProperty("id");
					// ////System.out.println(glyphId);
					if (glist.containsKey(glyphId))
						glyph = glist.get(glyphId);
					else { // ////System.out.println("gg " + glyphId);
						glyph = new Glyph();
					}

					glyph.setId(glyphId);			
					glyph.setClazz(((Node) row.get("g")).getLabels().iterator().next().toString().replaceAll("_", " "));

					if (!((String) ((Node) row.get("g")).getProperty("compRef")).equals("notexist")) {
						Glyph glyphforCompartment = new Glyph();
						comprefList.add((String) ((Node) row.get("g")).getProperty("compRef"));
						glyphforCompartment.setId((String) ((Node) row.get("g")).getProperty("compRef"));
						// buraya ekleme yapılacak comprefler çekilecek
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
								//// //System.out.println("ch " + childglyph.getId());
								set.add(childglyph.getId());
								glyphParent.getGlyph().add(childglyph);

							});
						}
						glist.putIfAbsent(glyphParent.getId(), glyphParent);				
					}
				}

			
				String queryComprefs = "Match (g)  where g.id in {comprefIds} return g";

				Map<String, Object> parametersComprefs = new HashMap<String, Object>();
				parametersComprefs.put("comprefIds", comprefList.toArray());
				Result resultCompRef = db.execute(queryComprefs, parametersComprefs);

				while (resultCompRef.hasNext()) {
					Map<String, Object> row = resultCompRef.next();

					Glyph glyph;
					String glyphId = (String) ((Node) row.get("g")).getProperty("id");
					// //System.out.println(glyphId);
					if (glist.containsKey(glyphId))
						glyph = glist.get(glyphId);
					else { // //System.out.println("gg " + glyphId);
						glyph = new Glyph();
					}

					glyph.setId(glyphId);			

					glyph.setClazz(((Node) row.get("g")).getLabels().iterator().next().toString().replaceAll("_", " "));

					if (!((String) ((Node) row.get("g")).getProperty("compRef")).equals("notexist")) {
						Glyph glyphforCompartment = new Glyph();
						comprefList.add((String) ((Node) row.get("g")).getProperty("compRef"));
						// //System.out.println((String) ((Node)
						// row.get("g")).getProperty("compRef"));
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
					glist.putIfAbsent(glyph.getId(), glyph);
				}
				
				String query = "MATCH (a)-[r]->(b) where r.startX is not null  and r in {rels} return a.id as sid,b.id as tid, r.startX as xx,r.startY as yy, r.endX as ex, r.endY as ey, r.aid as id, type(r) as class";

				Map<String, Object> parameters1 = new HashMap<String, Object>();

			    parameters1.put("rels", relsListPurify);

				Result result2 = db.execute(query, parameters1);

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
				String enes = writeToString(sss);
				return enes;
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
			
			String queryM = "match (a) where a.label in {lists}  optional match (a)-[:resideIn*]-(c)   return  collect(a.id) as idlist, collect(c.id) as cidlist";
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("lists", genesList.split(" "));

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
									
										String query3 = "match (po:Port) where po.id = {id}  optional match p1= (po)--(pr)--(:Port)--(a) where  pr:dissociation or  pr:process or pr:association or pr:omitted_process or pr:uncertain_process optional match p4=(a)-[:resideIn*]-(m) optional match p2 = (po)--(:process)--(b) where not b:port optional match p7= (po)--(:omitted_process)--(f) where not f:port optional match p8= (po)--(:uncertain_process)--(g) where not g:port optional match p5=(b)-[:resideIn*]-(n) optional match p3 = (po)--(c) where not c:process and not c:omitted_process and not c:uncertain_process and not c:association  and not c:dissociation optional match p6=(c)-[:resideIn*]-(v) return [a.id,b.id, c.id, f.id, g.id, m.id, n.id,v.id] as idList, [p1,p2,p3,p4,p5,p6,p7,p8] as pathList";
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
								}
								);
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
			
			String queryF = "Match (g)  where  not g:Port and g in {nodes} optional match (g)<-[:resideIn]-(gc) optional match (g)-[:hasPort]->(p:Port) optional match (g)-[:resideIn]->(prnt) optional match (sblg) - [:resideIn] -> (prnt) where sblg <> g return g, collect(gc) as childGlyph, collect(p) as ports, collect(distinct prnt) as prntList, collect(sblg) as sblgList, count(prnt) as  prntcount  order by prntcount desc";

			Map<String, Object> parametersF = new HashMap<String, Object>();
			parametersF.put("nodes", nodesList.values());

			Result resultF = db.execute(queryF, parametersF);

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
					// buraya ekleme yapılacak comprefler çekilecek
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
		
			String queryComprefs = "Match (g)  where g.id in {comprefIds} return g";

			Map<String, Object> parametersComprefs = new HashMap<String, Object>();
			parametersComprefs.put("comprefIds", comprefList.toArray());
			Result resultCompRef = db.execute(queryComprefs, parametersComprefs);

			while (resultCompRef.hasNext()) {
				Map<String, Object> row = resultCompRef.next();

				Glyph glyph;
				String glyphId = (String) ((Node) row.get("g")).getProperty("id");
				// //System.out.println(glyphId);
				if (glist.containsKey(glyphId))
					glyph = glist.get(glyphId);
				else { // //System.out.println("gg " + glyphId);
					glyph = new Glyph();
				}

				glyph.setId(glyphId);			
				glyph.setClazz(((Node) row.get("g")).getLabels().iterator().next().toString().replaceAll("_", " "));

				if (!((String) ((Node) row.get("g")).getProperty("compRef")).equals("notexist")) {
					Glyph glyphforCompartment = new Glyph();
					comprefList.add((String) ((Node) row.get("g")).getProperty("compRef"));
					// //System.out.println((String) ((Node)
					// row.get("g")).getProperty("compRef"));
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
				glist.putIfAbsent(glyph.getId(), glyph);
			}
			
			String query = "MATCH (a)-[r]->(b) where r.startX is not null  and r in {rels} return a.id as sid,b.id as tid, r.startX as xx,r.startY as yy, r.endX as ex, r.endY as ey, r.aid as id, type(r) as class";

			Map<String, Object> parameters1 = new HashMap<String, Object>();

			parameters1.put("rels", relsList.values());

			Result result2 = db.execute(query, parameters1);

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
				sbgnMap.getGlyph().add(value);;
				cnts++;
			}
			Sbgn sss = new Sbgn();
			sss.setMap(sbgnMap);
			String enes = writeToString(sss);
			return enes;
		}	

		private String PathsBetweenFunc(String genesList, String genesListTarget,double limita, double addition) throws JAXBException {			
			
			Transaction tx = null;		
		
			HashMap<String, Relationship> relsList = new HashMap<String, Relationship>();

			int direction =0;
			
			
			HashMap<String, Glyph> glist = new HashMap<String, Glyph>();
			HashMap<String, Port> portlist = new HashMap<String, Port>();
			org.sbgn.bindings.Map sbgnMap = new org.sbgn.bindings.Map();
			Set<String> set = new HashSet<>();
			Set<String> comprefList = new HashSet<String>();

			Set<Node> nodeList = new HashSet<Node>();		

				
				Set<Node> nodesListPurify = new HashSet<Node>(); 
				Set<Relationship> relsListPurify = new HashSet<Relationship>(); 
				
				String queryT = "match (a) where a.label in {lists}  optional match (a)-[:resideIn*]-(c)   return  collect(a.id) as idlist, collect(c.id) as cidlist";
			
				Map<String, Object> parametersT = new HashMap<String, Object>();
				parametersT.put("lists", genesListTarget.split(" "));

				Result resultT = db.execute(queryT, parametersT);

				Set<Object> idListTarget = new HashSet<Object>();
				while (resultT.hasNext()) {
					Map<String, Object> row = resultT.next();
					idListTarget.addAll((List<Object>) row.get("idlist"));
					idListTarget.addAll((List<Object>) row.get("cidlist"));
				}			
			
			String queryM = "match (a) where a.label in {lists}  optional match (a)-[:resideIn*]-(c)   return  collect(a.id) as idlist, collect(c.id) as cidlist";
		
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("lists", genesList.split(" "));
			
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
					
			for(Object idListItem : idList){
			
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
				PathListITem rootItem =  new PathListITem(idListItem.toString(), 0);
				rootItem.pathNodeIdList.add(idListItem.toString());
				AddToFreshPidTupleList(root.FreshIdMap, idListWithSiblings, "");
				root.PathListContainer.put(idListItem.toString()+":0.0", rootItem);
			
				PathsBetweenRootList.add(root);		
				}
				
				double count=0;
			
				while (count< limita && count < shortestlength +addition) {				

						for(PathsBetweenRoot root : PathsBetweenRootList){
							
							root.FreshIdListToIterate.removeAll(root.OldIdListToIterate);

							String query = "match p= (a)--(b)  where a.id in {lists} optional match p2 = (b)-[:resideIn*]-(c) return a.id as pid, [b, c] as blist, [p , p2] as p2List";
							Map<String, Object> parametersr = new HashMap<String, Object>();
							parametersr.put("lists", root.FreshIdListToIterate);

							
							Result result2 = db.execute(query, parametersr);
							root.OldIdListToIterate.addAll(root.FreshIdListToIterate);
							root.FreshIdListToIterate.clear();
							
							while (result2.hasNext()) {
								Map<String, Object> row = result2.next();

								String pid= (String) row.get("pid");						
								
								// TODO diger proclara uygula
								root.pathList.addAll((List<Path>) row.get("p2List"));
								Set<Node> nodebLists = new HashSet<Node>((List<Node>) row.get("blist"));
								
								
								for (Node blistitem :nodebLists ){

									if (blistitem != null) {
									
										blistitem.getLabels().forEach(blistitemlabel -> {					

											if (blistitemlabel.name().equals("Port")) {								

												String query3 = "";
												if (direction == 0) {
													query3 = "match (po:Port) where po.id = {id}  optional match p1= (po)--(pr)--(:Port)--(a) where not a:Port and  (pr:dissociation or  pr:process or pr:association or pr:omitted_process or pr:uncertain_process) optional match p4=(a)-[:resideIn*]-(m) optional match p2 = (po)--(:process)--(b) where not b:Port optional match p7= (po)--(:omitted_process)--(f) where not f:port optional match p8= (po)--(:uncertain_process)--(g) where not g:port optional match p5=(b)-[:resideIn*]-(n) optional match p3 = (po)--(c) where not c:process and not c:omitted_process and not c:uncertain_process and not c:association  and not c:dissociation optional match p6=(c)-[:resideIn*]-(k) return [a.id,b.id, c.id, f.id, g.id, m.id, n.id, k.id] as idList, [p1,p2,p3,p4,p5,p6,p7,p8] as pathList";

												} else if (direction == 1) {
													query3 = "match (po:Port) where po.id = {id}  optional match p1= (po)--(pr)--(:Port)-[]->(a) where  pr:dissociation or  pr:process or pr:association or pr:omitted_process or pr:uncertain_process optional match p4=(a)-[:resideIn*]-(m) optional match p2 = (po)--(:process)-[]->(b) where not b:Port optional match p7= (po)--(:omitted_process)-[]->(f) where not f:port optional match p8= (po)--(:uncertain_process)-[]->(g) where not g:port optional match p5=(b)-[:resideIn*]-(n) optional match p3 = (po)-[]->(c) where not c:process and not c:omitted_process and not c:uncertain_process and not c:association  and not c:dissociation optional match p6=(c)-[:resideIn*]-(k) return [a.id,b.id, c.id, f.id, g.id, m.id, n.id, k.id] as idList, [p1,p2,p3,p4,p5,p6,p7,p8] as pathList";

												} else if (direction == 2) {
													query3 = "match (po:Port) where po.id = {id}  optional match p1= (po)--(pr)--(:Port)<-[]-(a) where  pr:dissociation or  pr:process or pr:association or pr:omitted_process or pr:uncertain_process optional match p4=(a)-[:resideIn*]-(m) optional match p2 = (po)--(:process)<-[]-(b) where not b:Port optional match p7= (po)--(:omitted_process)<-[]-(f) where not f:port optional match p8= (po)--(:uncertain_process)<-[]-(g) where not g:port optional match p5=(b)-[:resideIn*]-(n) optional match p3 = (po)<-[]-(c) where not c:process and not c:omitted_process and not c:uncertain_process and not c:association  and not c:dissociation optional match p6=(c)-[:resideIn*]-(k) return [a.id,b.id, c.id, f.id, g.id, m.id, n.id, k.id] as idList, [p1,p2,p3,p4,p5,p6,p7,p8] as pathList";

												}
												Map<String, Object> parameters3 = new HashMap<String, Object>();
												
												parameters3.put("id", blistitem.getProperty("id"));

												Result result3 = db.execute(query3, parameters3);
												
												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();

													root.FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
													AddToFreshPidTupleList(root.FreshIdMap, (List<Object>) row3.get("idList"),pid);											
													root.pathList.addAll((List<Path>) row3.get("pathList"));												
												}
												
											} else if (blistitemlabel.name().equals("process")) {
												
												String query3 = "";
												if (direction == 1) {
													query3 = "match (p:process) where p.id = {id} optional match p1= (p)--(:Port)-[]->(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)-[]->(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												} else if (direction == 2) {
													query3 = "match (p:process) where p.id = {id} optional match p1= (p)--(:Port)<-[]-(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)<-[]-(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												} else if (direction == 0) {
													query3 = "match (p:process) where p.id = {id} optional match p1= (p)--(:Port)--(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)--(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												}
												Map<String, Object> parameters3 = new HashMap<String, Object>();

												parameters3.put("id", blistitem.getProperty("id"));

												Result result3 = db.execute(query3, parameters3);
												
												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();
													
													root.FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
													AddToFreshPidTupleList(root.FreshIdMap, (List<Object>) row3.get("idList"),pid);
													root.pathList.addAll((List<Path>) row3.get("pathList"));						
											
												}
											} else if (blistitemlabel.name().equals("omitted_process")) {
											
												String query3 = "";
												if (direction == 1) {
													query3 = "match (p:omitted_process) where p.id = {id} optional match p1= (p)--(:Port)-[]->(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)-[]->(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id]as idList,  [p1,p2,p3,p4] as pathList";
												} else if (direction == 2) {
													query3 = "match (p:omitted_process) where p.id = {id} optional match p1= (p)--(:Port)<-[]-(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)<-[]-(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												} else if (direction == 0) {
													query3 = "match (p:omitted_process) where p.id = {id} optional match p1= (p)--(:Port)--(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)--(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												}
												
												Map<String, Object> parameters3 = new HashMap<String, Object>();
												
												parameters3.put("id", blistitem.getProperty("id"));
												Result result3 = db.execute(query3, parameters3);
											
												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();		
													AddToFreshPidTupleList(root.FreshIdMap, (List<Object>) row3.get("idList"),pid);
													root.FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));										
													root.pathList.addAll((List<Path>) row3.get("pathList"));
												}

											} else if (blistitemlabel.name().equals("uncertain_process")) {

												String query3 = "";
												if (direction == 1) {
													query3 = "match (p:uncertain_process) where p.id = {id} optional match p1= (p)--(:Port)-[]->(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)-[]->(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												} else if (direction == 2) {
													query3 = "match (p:uncertain_process) where p.id = {id} optional match p1= (p)--(:Port)<-[]-(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)<-[]-(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												} else if (direction == 0) {
													query3 = "match (p:uncertain_process) where p.id = {id} optional match p1= (p)--(:Port)--(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)--(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												}
												Map<String, Object> parameters3 = new HashMap<String, Object>();

												parameters3.put("id", blistitem.getProperty("id"));
												Result result3 = db.execute(query3, parameters3);
												
												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();		
													AddToFreshPidTupleList(root.FreshIdMap, (List<Object>) row3.get("idList"),pid);
													root.FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));										
													root.pathList.addAll((List<Path>) row3.get("pathList"));									

												}

											} else if (blistitemlabel.name().equals("association")) {
												
												String query3 = "";
												if (direction == 1) {
													query3 = "match (asc:association) where asc.id = {id}  optional match p1= (asc)--(:Port)-[]->(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

												} else if (direction == 2) {
													query3 = "match (asc:association) where asc.id = {id}  optional match p1= (asc)--(:Port)<-[]-(a) optional match p2=(a)-[:resideIn*]-(b)  return [a.id, b.id] as idList, [p1,p2] as pathList";

												} else if (direction == 0) {
													query3 = "match (asc:association) where asc.id = {id}  optional match p1= (asc)--(:Port)--(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

												}

												Map<String, Object> parameters3 = new HashMap<String, Object>();
											
												parameters3.put("id", blistitem.getProperty("id"));

												Result result3 = db.execute(query3, parameters3);

												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();
													AddToFreshPidTupleList(root.FreshIdMap, (List<Object>) row3.get("idList"),pid);
													root.FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
													root.pathList.addAll((List<Path>) row3.get("pathList"));
													
												}

											} else if (blistitemlabel.name().equals("dissociation")) {							
												
												String query3 = "";
												if (direction == 1) {
													query3 = "match (asc:dissociation) where asc.id = {id}  optional match p1= (asc)--(:Port)-[]->(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

												} else if (direction == 2) {
													query3 = "match (asc:dissociation) where asc.id = {id}  optional match p1= (asc)--(:Port)<-[]-(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

												} else if (direction == 0) {
													query3 = "match (asc:dissociation) where asc.id = {id}  optional match p1= (asc)--(:Port)--(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

												}
												Map<String, Object> parameters3 = new HashMap<String, Object>();

												parameters3.put("id", blistitem.getProperty("id"));

												Result result3 = db.execute(query3, parameters3);

												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();												
													AddToFreshPidTupleList(root.FreshIdMap, (List<Object>) row3.get("idList"),pid);
													root.FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
													root.pathList.addAll((List<Path>) row3.get("pathList"));										
												}
											}

											else {											
												root.FreshIdListToIterate.add(blistitem.getProperty("id"));											
												List<Object> lst = new ArrayList<Object>();
												lst.add(blistitem.getProperty("id"));
												AddToFreshPidTupleList(root.FreshIdMap, lst,pid);									
												root.pathList.addAll((List<Path>) row.get("p2List"));
											}

											if (blistitemlabel.name().equals("complex")) {

												String query3 = "match  (c:complex) where c.id = {id}  optional match p1= (c)-[:resideIn*]-(a)   unwind nodes(p1) as p1Node with DISTINCT p1Node, p1  return collect(p1Node.id) as idList,  [p1] as pathList";
												Map<String, Object> parameters3 = new HashMap<String, Object>();
												
												parameters3.put("id", blistitem.getProperty("id"));

												Result result3 = db.execute(query3, parameters3);
												
												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();
													root.FreshIdListToIterate.addAll((List<Object>)row3.get("idList"));
													AddToFreshPidTupleList(root.FreshIdMap, (List<Object>) row3.get("idList"),pid);		
													root.pathList.addAll((List<Path>) row3.get("pathList"));									
														
												}
												
											}				
										}
										);
									}						
							
								}							
							}
							
							for(FreshIdItem freshid : 	root.FreshIdMap.values()){
									double compkey = count +1;
									if(freshid !=null){
										
										PathListITem pitem = root.PathListContainer.get(freshid.pid.toString()+":"+count);
										
												if(pitem != null && !freshid.pid.equals("") ){
																								
													if(pitem.endNodeDepth +1 == count +1 && pitem.pathNodeIdList.size()== count +1 ){
														PathListITem fitem  = new PathListITem(freshid.sid.toString(),compkey);											
													fitem.pathNodeIdList.addAll(pitem.pathNodeIdList);
													fitem.pathNodeIdList.add(freshid.sid.toString());										
												
															root.PathListContainer.putIfAbsent(freshid.sid.toString()+":"+compkey , fitem);
													
												}
											}
									
									}
									
							}
							
							
							if(shortestlength == 9999 && !Collections.disjoint(root.FreshIdListToIterate, idListTarget) ){
								shortestlength = count  +1;									
							}
							
						}					
					count++;
				}	
			for(PathsBetweenRoot rootItem :PathsBetweenRootList  ){
				
						
						for(PathListITem ptlItem : rootItem.PathListContainer.values()){
							
							if(idListTarget.contains(ptlItem.endNodeId)){
								
								String queryToGetCompoundNodes = "match (a) where a.id in {SourceidList} optional match p1= (a)-[:resideIn*]-(b) unwind nodes(p1) as p1Node   return collect(p1Node.id) as cmpndNodes"  ;
								
								Map<String, Object> parametersGetCompoundNodes = new HashMap<String, Object>();
						
								parametersGetCompoundNodes.put("SourceidList", ptlItem.pathNodeIdList);						
								Set<String> pathNodeIdList= new HashSet<String>();
								Result resultGetCompoundNodes = db.execute(queryToGetCompoundNodes, parametersGetCompoundNodes);
								Set<Node> NodeListForPurifyQuery = new HashSet<Node>();
								while (resultGetCompoundNodes.hasNext()) {							
									Map<String, Object> rowGetCompoundNodes = resultGetCompoundNodes.next();
									pathNodeIdList.addAll((List<String>) rowGetCompoundNodes.get("cmpndNodes"));		
							
									
								}
								pathNodeIdList.addAll(ptlItem.pathNodeIdList);							
							
								Set<Relationship> allRelationsgci = new HashSet<Relationship>(); 
								Set<Object> nodes = new HashSet<Object>(); 
								HashMap<String, Node> nodesList = new HashMap<String, Node>();
								
								rootItem.pathList.forEach(pathItem -> {

									if (pathItem != null) {			
										
										pathItem.nodes().forEach(nodeItem -> {
											if (nodeItem != null){										
												
												for(org.neo4j.graphdb.Label lbl : nodeItem.getLabels()){
													
													if(lbl.name().equals("macromolecule") || lbl.name().equals("macromolecule")  ){
													String nodeID =nodeItem.getProperty("id").toString();
														if(pathNodeIdList.contains(nodeID)){
														nodesList.putIfAbsent(nodeID, nodeItem);}
													}
													else{
														nodesList.putIfAbsent(nodeItem.getProperty("id").toString(), nodeItem);
														
													}
												}
												
											}
												

										});
									}
								});
									
								String queryToPurify = "match (a) where a.id = {Sourceid} with a  optional match (b)  where  b.id in {finalNodes} with a, b optional match  p= (a)-[r*]-(b)  where  all(rv IN  nodes(p)  WHERE rv in {nodes} )  unwind nodes(p) as pNode with DISTINCT pNode, p optional match p2= (pNode)-[:resideIn*]-(ac)  return [p,p2] as pathList" ;
								
								Map<String, Object> parametersPurify = new HashMap<String, Object>();
							    
								parametersPurify.put("Sourceid",rootItem.nodeId);
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
													relsListPurify.add( relitem);
											});
										}
									}
								});							
							
							}
						}
			}		
				
			String queryF = "Match (g)  where  not g:Port and g in {nodes} optional match (g)<-[:resideIn]-(gc) optional match (g)-[:hasPort]->(p:Port) optional match (g)-[:resideIn]->(prnt) optional match (sblg) - [:resideIn] -> (prnt) where sblg <> g return g, collect(gc) as childGlyph, collect(p) as ports, collect(distinct prnt) as prntList, collect(sblg) as sblgList, count(prnt) as  prntcount  order by prntcount desc";

			Map<String, Object> parametersF = new HashMap<String, Object>();
			parametersF.put("nodes", nodesListPurify);

			Result resultF = db.execute(queryF, parametersF);

			while (resultF.hasNext()) {
				Map<String, Object> row = resultF.next();

				List<Node> ports = (List<Node>) row.get("ports");
				List<Node> childGlyphs = (List<Node>) row.get("childGlyph");

				Glyph glyph;
				String glyphId = (String) ((Node) row.get("g")).getProperty("id");
				// ////System.out.println(glyphId);
				if (glist.containsKey(glyphId))
					glyph = glist.get(glyphId);
				else { // ////System.out.println("gg " + glyphId);
					glyph = new Glyph();
				}

				glyph.setId(glyphId);			

				glyph.setClazz(((Node) row.get("g")).getLabels().iterator().next().toString().replaceAll("_", " "));

				if (!((String) ((Node) row.get("g")).getProperty("compRef")).equals("notexist")) {
					Glyph glyphforCompartment = new Glyph();
					comprefList.add((String) ((Node) row.get("g")).getProperty("compRef"));

					glyphforCompartment.setId((String) ((Node) row.get("g")).getProperty("compRef"));
					// buraya ekleme yapılacak comprefler çekilecek
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
						// ////System.out.println("gc " + childglyphId);
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
						//// //System.out.println("aa " + (String)
						//// item.getProperty("compRef"));
						comprefList.add((String) item.getProperty("compRef"));
						Glyph cglyphforCompartment = new Glyph();
						cglyphforCompartment.setId((String) item.getProperty("compRef"));
						childglyph.setCompartmentRef(cglyphforCompartment);

					}

					glist.putIfAbsent(childglyph.getId(), childglyph);
					//// //System.out.println("ch "+ childglyph.getId());
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
					// ////System.out.println(glyphId);
					if (glist.containsKey(glyphParentId))
						glyphParent = glist.get(glyphParentId);
					else { // ////System.out.println("gg " + glyphId);
						glyphParent = new Glyph();

						glyphParent.setId(glyphParentId);					

						glyphParent.setClazz(parent.getLabels().iterator().next().toString().replaceAll("_", " "));

						if (!((String) parent.getProperty("compRef")).equals("notexist")) {
							Glyph glyphforCompartmentp = new Glyph();
							comprefList.add((String) parent.getProperty("compRef"));
							//// //System.out.println((String)
							//// parent.getProperty("compRef"));
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
								// ////System.out.println("gc " + childglyphId);
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
								//// //System.out.println("aa " + (String)
								//// item.getProperty("compRef"));
								comprefList.add((String) item.getProperty("compRef"));
								Glyph cglyphforCompartment = new Glyph();
								cglyphforCompartment.setId((String) item.getProperty("compRef"));
								childglyph.setCompartmentRef(cglyphforCompartment);

							}

							glist.putIfAbsent(childglyph.getId(), childglyph);
							//// //System.out.println("ch " + childglyph.getId());
							set.add(childglyph.getId());
							glyphParent.getGlyph().add(childglyph);

						});
					}
					glist.putIfAbsent(glyphParent.getId(), glyphParent);
					// ////System.out.println("gg "+ glyph.getId());
					/// glyph.setId( );
				}

			}

			// burada cmprefler in comparmentlar çekielcek
			String queryComprefs = "Match (g)  where g.id in {comprefIds} return g";

			Map<String, Object> parametersComprefs = new HashMap<String, Object>();
			parametersComprefs.put("comprefIds", comprefList.toArray());
			Result resultCompRef = db.execute(queryComprefs, parametersComprefs);

			while (resultCompRef.hasNext()) {
				Map<String, Object> row = resultCompRef.next();

				Glyph glyph;
				String glyphId = (String) ((Node) row.get("g")).getProperty("id");
				// //System.out.println(glyphId);
				if (glist.containsKey(glyphId))
					glyph = glist.get(glyphId);
				else { // //System.out.println("gg " + glyphId);
					glyph = new Glyph();
				}
				glyph.setId(glyphId);
				
				glyph.setClazz(((Node) row.get("g")).getLabels().iterator().next().toString().replaceAll("_", " "));

				if (!((String) ((Node) row.get("g")).getProperty("compRef")).equals("notexist")) {
					Glyph glyphforCompartment = new Glyph();
					comprefList.add((String) ((Node) row.get("g")).getProperty("compRef"));
					// //System.out.println((String) ((Node)
					// row.get("g")).getProperty("compRef"));
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
			
			String query = "MATCH (a)-[r]->(b) where r.startX is not null  and r in {rels} return a.id as sid,b.id as tid, r.startX as xx,r.startY as yy, r.endX as ex, r.endY as ey, r.aid as id, type(r) as class";

			Map<String, Object> parameters1 = new HashMap<String, Object>();

			parameters1.put("rels",relsListPurify);

			Result result2 = db.execute(query, parameters1);

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
		
			glist.keySet().removeAll(set);
			for (Iterator<Glyph> iterator = glist.values().iterator(); iterator.hasNext();) {
				Glyph value = iterator.next();
				sbgnMap.getGlyph().add(value);			
			}
			
			Sbgn sss = new Sbgn();
			sss.setMap(sbgnMap);
			String enes = writeToString(sss);
			return enes;
		}

		public  String GoIfunc(String genesList, double limita, double direction) throws JAXBException {

			int limit;
			//int limita = (int)limitk;
			File storeDir = new File(
						"C:\\Users\\user\\.Neo4jDesktop\\neo4jDatabases\\database-9ce658b8-be04-4695-bf84-a51622d0e74a\\installation-3.3.3\\data\\databases\\graph.db");
				GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);
				//System.out.println("basla");
				Transaction tx = null;
		
				// HashMap<String, Node> nodesList = new HashMap<String, Node>();
				// HashMap<String, Relationship> relsList = new HashMap<String,
				// Relationship>();
				try {

					tx = graphDb.beginTx();
			Set<Node> nodesListPurify = new HashSet<Node>(); 
			Set<Relationship> relsListPurify = new HashSet<Relationship>(); 
				ArrayList<GOINodeContainer> GOINodeContainerList = new ArrayList<GOINodeContainer>();
				Glyph a;
				HashMap<String, Glyph> glist = new HashMap<String, Glyph>();
				HashMap<String, Port> portlist = new HashMap<String, Port>();
				org.sbgn.bindings.Map sbgnMap = new org.sbgn.bindings.Map();
				Set<String> set = new HashSet<>();
				Set<String> comprefList = new HashSet<String>();
				Set<Relationship> allRelations = new HashSet<Relationship>(); 
				

					Set<Node> nodeList = new HashSet<Node>();
					// verilen glyplere ait idleri al
					String queryM = "match (a) where a.label in {lists}  return  collect(a.id) as idlist";

					Map<String, Object> parameters = new HashMap<String, Object>();
					parameters.put("lists", genesList.split(" "));
					

					Result result = graphDb.execute(queryM, parameters);

					// Set<Path> pathList = new HashSet<Path>();
					Set<Object> idList = new HashSet<Object>();
					while (result.hasNext()) {
						Map<String, Object> row = result.next();
						idList.addAll((List<Object>) row.get("idlist"));
											
					}

					
					for(Object idListItem : idList){
						
						
						//GOINodeContainer
						GOINodeContainer gOINodeContainer = new GOINodeContainer();
						gOINodeContainer.rootId= (String)idListItem;
						
						String queryM2 = "match (a) where a.id = {id}  optional match (a)-[:resideIn*]-(c) where not c:state_variable  return  collect(a.id) as idlist, collect(c.id) as cidlist";
						
						Map<String, Object> parameterse = new HashMap<String, Object>();
						parameterse.put("id", idListItem);


						Result resulte = graphDb.execute(queryM2, parameterse);

						// Set<Path> pathList = new HashSet<Path>();
						Set<Object> idListWithSiblings = new HashSet<Object>();
						while (resulte.hasNext()) {
							Map<String, Object> row = resulte.next();
							idListWithSiblings.addAll((List<Object>) row.get("idlist"));
							//System.out.println("s " + idListWithSiblings);
							idListWithSiblings.addAll((List<Object>) row.get("cidlist"));
							//System.out.println("s " + idListWithSiblings);
						}
						idListWithSiblings.forEach(idsblitem ->{
							//gOINodeContainer.GOINodeObjectList.putIfAbsent((String) idsblitem, new GOINodeObject((String)idsblitem,0,null));
							
						});
						
						// her leveldaki start nodeları id list içinde tutuyorum.
						Set<Object> FreshIdListToIterate = new HashSet<Object>();
						Set<Object> OldIdListToIterate = new HashSet<Object>();
						FreshIdListToIterate.addAll(idListWithSiblings);
						 limit = (int) limita;
						//int depth =0;
						while (limit > 0) {
							//System.out.println("buss");

							FreshIdListToIterate.removeAll(OldIdListToIterate);
							String query = "";

							if (direction == 0) {
								query = "match p= (a)--(b) where a.id in {lists} optional match p2 = (b)-[:resideIn*]-(c) return [b, c] as blist, [p , p2] as p2List";
							} else if (direction == 1) {
								query = "match p= (a)-[]->(b) where a.id in {lists} optional match p2 = (b)-[:resideIn*]-(c) return [b, c] as blist, [p , p2] as p2List";
							} else if (direction == 2) {
								query = "match p= (a)<-[]-(b) where a.id in {lists} optional match p2 = (b)-[:resideIn*]-(c) return [b, c] as blist, [p , p2] as p2List";
							}

							Map<String, Object> parametersr = new HashMap<String, Object>();
							parametersr.put("lists", FreshIdListToIterate);

							Result result2 = graphDb.execute(query, parametersr);
							OldIdListToIterate.addAll(FreshIdListToIterate);
							FreshIdListToIterate.clear();

							while (result2.hasNext()) {
								Map<String, Object> row = result2.next();

								// blist içinde yer alan nodeların listesi
								Set<Node> nodebLists = new HashSet<Node>((List<Node>) row.get("blist"));
								// ************ CHECK IT
								gOINodeContainer.pathList.addAll((List<Path>) row.get("p2List"));
								
								//GoiNodePathExtract(gOINodeContainer, (List<Object>) row3.get("idList"),(List<Path>) row3.get("pathList"), limita - limit);
								
							
								for(Node blistitem : nodebLists){
									
									if(blistitem != null){


										
										
										for(org.neo4j.graphdb.Label blistitemlabel : blistitem.getLabels()) {
											
											if (blistitemlabel.name().equals("Port")) {

												String query3 = "";
												if (direction == 0) {
													query3 = "match (po:Port) where po.id = {id}  optional match p1= (po)--(pr)--(:Port)--(a) where  pr:dissociation or  pr:process or pr:association or pr:omitted_process or pr:uncertain_process optional match p4=(a)-[:resideIn*]-(m) optional match p2 = (po)--(:process)--(b) where not b:port optional match p7= (po)--(:omitted_process)--(f) where not f:port optional match p8= (po)--(:uncertain_process)--(g) where not g:port optional match p5=(b)-[:resideIn*]-(n) optional match p3 = (po)--(c) where not c:process and not c:omitted_process and not c:uncertain_process and not c:association  and not c:dissociation optional match p6=(c)-[:resideIn*]-(k) return [a.id,b.id, c.id, f.id, g.id, m.id, n.id, k.id] as idList, [p1,p2,p3,p4,p5,p6,p7,p8] as pathList";

												} else if (direction == 1) {
													query3 = "match (po:Port) where po.id = {id}  optional match p1= (po)--(pr)--(:Port)-[]->(a) where  pr:dissociation or  pr:process or pr:association or pr:omitted_process or pr:uncertain_process optional match p4=(a)-[:resideIn*]-(m) optional match p2 = (po)--(:process)-[]->(b) where not b:port optional match p7= (po)--(:omitted_process)-[]->(f) where not f:port optional match p8= (po)--(:uncertain_process)-[]->(g) where not g:port optional match p5=(b)-[:resideIn*]-(n) optional match p3 = (po)-[]->(c) where not c:process and not c:omitted_process and not c:uncertain_process and not c:association  and not c:dissociation optional match p6=(c)-[:resideIn*]-(k) return [a.id,b.id, c.id, f.id, g.id, m.id, n.id, k.id] as idList, [p1,p2,p3,p4,p5,p6,p7,p8] as pathList";

												} else if (direction == 2) {
													query3 = "match (po:Port) where po.id = {id}  optional match p1= (po)--(pr)--(:Port)<-[]-(a) where  pr:dissociation or  pr:process or pr:association or pr:omitted_process or pr:uncertain_process optional match p4=(a)-[:resideIn*]-(m) optional match p2 = (po)--(:process)<-[]-(b) where not b:port optional match p7= (po)--(:omitted_process)<-[]-(f) where not f:port optional match p8= (po)--(:uncertain_process)<-[]-(g) where not g:port optional match p5=(b)-[:resideIn*]-(n) optional match p3 = (po)<-[]-(c) where not c:process and not c:omitted_process and not c:uncertain_process and not c:association  and not c:dissociation optional match p6=(c)-[:resideIn*]-(k) return [a.id,b.id, c.id, f.id, g.id, m.id, n.id, k.id] as idList, [p1,p2,p3,p4,p5,p6,p7,p8] as pathList";

												}
												// port u geç ve uygun nodları level1
												// elemanı gibi varsay
												Map<String, Object> parameters3 = new HashMap<String, Object>();

												// port id verildi
												parameters3.put("id", blistitem.getProperty("id"));

												Result result3 = graphDb.execute(query3, parameters3);

												// porttan dönen uygun level1 pathları ve
												// idleri result3 ile dönecek
												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();

													FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));

													//// //System.out.println("portin" +
													//// (List<Object>) row3.get("idList"));
													//gOINodeContainer.pathList.addAll((List<Path>) row3.get("pathList"));
													
												//	System.out.println("port 1");
													double depth3 = limita - limit;
													GoiNodePathExtract(gOINodeContainer, (List<String>) row3.get("idList"),(List<Path>) row3.get("pathList"), depth3);
													//System.out.println("port 2");
												}

											
											} else if (blistitemlabel.name().equals("process")) {

												// process u geç ve uygun nodları level1
												// elemanı gibi varsay

												String query3 = "";
												if (direction == 1) {
													query3 = "match (p:process) where p.id = {id} optional match p1= (p)--(:Port)-[]->(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)-[]->(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												} else if (direction == 2) {
													query3 = "match (p:process) where p.id = {id} optional match p1= (p)--(:Port)<-[]-(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)<-[]-(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												} else if (direction == 0) {
													query3 = "match (p:process) where p.id = {id} optional match p1= (p)--(:Port)--(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)--(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												}

												Map<String, Object> parameters3 = new HashMap<String, Object>();

												// process id verildi
												parameters3.put("id", blistitem.getProperty("id"));

												Result result3 = graphDb.execute(query3, parameters3);

												// processden dönen uygun level1 pathları ve
												// idleri result3 ile dönecek
												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();
													// ////System.out.println("processin "+
													// blistitem.getProperty("id") +" " +
													// (List<Object>) row3.get("idList"));

													FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
													// ////System.out.println("processin2 "+
													// blistitem.getProperty("id") +" " +
													// (List<Object>) row3.get("idList"));
													//gOINodeContainer.pathList.addAll((List<Path>) row3.get("pathList"));
													double depth3 = limita - limit;
													GoiNodePathExtract(gOINodeContainer, (List<String>) row3.get("idList"),(List<Path>) row3.get("pathList"), depth3);

												}
											} else if (blistitemlabel.name().equals("omitted_process")) {

											
												String query3 = "";
												if (direction == 1) {
													query3 = "match (p:omitted_process) where p.id = {id} optional match p1= (p)--(:Port)-[]->(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)-[]->(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id]as idList,  [p1,p2,p3,p4] as pathList";
												} else if (direction == 2) {
													query3 = "match (p:omitted_process) where p.id = {id} optional match p1= (p)--(:Port)<-[]-(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)<-[]-(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												} else if (direction == 0) {
													query3 = "match (p:omitted_process) where p.id = {id} optional match p1= (p)--(:Port)--(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)--(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												}

												Map<String, Object> parameters3 = new HashMap<String, Object>();

												// process id verildi
												parameters3.put("id", blistitem.getProperty("id"));

												Result result3 = graphDb.execute(query3, parameters3);

												// omitted_process dönen uygun level1
												// pathları
												// ve
												// idleri result3 ile dönecek
												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();
												

													FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
													
													//gOINodeContainer.pathList.addAll((List<Path>) row3.get("pathList"));
													double depth3 = limita - limit;
													GoiNodePathExtract(gOINodeContainer, (List<String>) row3.get("idList"),(List<Path>) row3.get("pathList"), depth3);

												}

											} else if (blistitemlabel.name().equals("uncertain_process")) {

												// process u geç ve uygun nodları level1
												// elemanı gibi varsay
												
												String query3 = "";
												if (direction == 1) {
													query3 = "match (p:uncertain_process) where p.id = {id} optional match p1= (p)--(:Port)-[]->(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)-[]->(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												} else if (direction == 2) {
													query3 = "match (p:uncertain_process) where p.id = {id} optional match p1= (p)--(:Port)<-[]-(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)<-[]-(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												} else if (direction == 0) {
													query3 = "match (p:uncertain_process) where p.id = {id} optional match p1= (p)--(:Port)--(a) optional match p3=(a)-[:resideIn*]-(c) optional match p2=(p)--(b) where not b:Port optional match p4=(b)-[:resideIn*]-(d) return [a.id, b.id, c.id ,d.id] as idList,  [p1,p2,p3,p4] as pathList";
												}

												Map<String, Object> parameters3 = new HashMap<String, Object>();

												// process id verildi
												parameters3.put("id", blistitem.getProperty("id"));

												Result result3 = graphDb.execute(query3, parameters3);

												// omitted_process dönen uygun level1
												// pathları
												// ve
												// idleri result3 ile dönecek
												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();
													
													FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
												
													//gOINodeContainer.pathList.addAll((List<Path>) row3.get("pathList"));
													double depth3 = limita - limit;
													GoiNodePathExtract(gOINodeContainer, (List<String>) row3.get("idList"),(List<Path>) row3.get("pathList"), depth3);

												}

											} else if (blistitemlabel.name().equals("association")) {

												

												String query3 = "";
												if (direction == 1) {
													query3 = "match (asc:association) where asc.id = {id}  optional match p1= (asc)--(:Port)-[]->(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

												} else if (direction == 2) {
													query3 = "match (asc:association) where asc.id = {id}  optional match p1= (asc)--(:Port)<-[]-(a) optional match p2=(a)-[:resideIn*]-(b)  return [a.id, b.id] as idList, [p1,p2] as pathList";

												} else if (direction == 0) {
													query3 = "match (asc:association) where asc.id = {id}  optional match p1= (asc)--(:Port)--(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

												}

												Map<String, Object> parameters3 = new HashMap<String, Object>();

												// association id verildi
												parameters3.put("id", blistitem.getProperty("id"));

												Result result3 = graphDb.execute(query3, parameters3);

												// association dönen uygun level1 pathları
												// ve
												// idleri result3 ile dönecek
												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();

													FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
													//gOINodeContainer.pathList.addAll((List<Path>) row3.get("pathList"));
													double depth3 = limita - limit;
													GoiNodePathExtract(gOINodeContainer, (List<String>) row3.get("idList"),(List<Path>) row3.get("pathList"), depth3);

												}

											} else if (blistitemlabel.name().equals("dissociation")) {

												String query3 = "";
												if (direction == 1) {
													query3 = "match (asc:dissociation) where asc.id = {id}  optional match p1= (asc)--(:Port)-[]->(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

												} else if (direction == 2) {
													query3 = "match (asc:dissociation) where asc.id = {id}  optional match p1= (asc)--(:Port)<-[]-(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

												} else if (direction == 0) {
													query3 = "match (asc:dissociation) where asc.id = {id}  optional match p1= (asc)--(:Port)--(a) optional match p2=(a)-[:resideIn*]-(b)  return  [a.id, b.id] as idList, [p1,p2] as pathList";

												}
												Map<String, Object> parameters3 = new HashMap<String, Object>();

												// association id verildi
												parameters3.put("id", blistitem.getProperty("id"));

												Result result3 = graphDb.execute(query3, parameters3);

												// association dönen uygun level1 pathları
												// ve
												// idleri result3 ile dönecek
												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();

													FreshIdListToIterate.addAll((List<Object>) row3.get("idList"));
													//gOINodeContainer.pathList.addAll((List<Path>) row3.get("pathList"));
												double depth3 = limita - limit;
													GoiNodePathExtract(gOINodeContainer, (List<String>) row3.get("idList"),(List<Path>) row3.get("pathList"), depth3);
												}

											}

											else {
												FreshIdListToIterate.add(blistitem.getProperty("id"));
												
												List<String> tempforuseasparamater = new ArrayList<String>();
												tempforuseasparamater.add((String)blistitem.getProperty("id"));
												
												List<Path> tempforuseasparamater2 = new ArrayList<Path>();
												tempforuseasparamater2.add((Path) row.get("p"));
												double depth3 = limita - limit;
												GoiNodePathExtract(gOINodeContainer, tempforuseasparamater,tempforuseasparamater2, depth3);
												
												// //System.out.println("buradamı "+
												// blistitem.getProperty("id"));
												//gOINodeContainer.pathList.add((Path) row.get("p"));
											}

											if (blistitemlabel.name().equals("complex")) {

											
												String query3 = "match  (c:complex) where c.id = {id}  optional match p1= (c)-[:resideIn*]-(a)  return  [p1] as pathList";

												Map<String, Object> parameters3 = new HashMap<String, Object>();

												// association id verildi
												parameters3.put("id", blistitem.getProperty("id"));

												Result result3 = graphDb.execute(query3, parameters3);

												while (result3.hasNext()) {
													Map<String, Object> row3 = result3.next();

													gOINodeContainer.pathList.addAll((List<Path>) row3.get("pathList"));
													
													
													//GoiNodePathExtract(gOINodeContainer, (List<Object>) row3.get("idList"),(List<Path>) row3.get("pathList"), limita - limit);
												}

											}
										}	
									}						
									
								}

							}
							limit--;
							
						double depth2 = limita - limit;
							FreshIdListToIterate.forEach(FreshIdListItem ->{
							gOINodeContainer.GOINodeObjectList.putIfAbsent((String) FreshIdListItem, new GOINodeObject((String)FreshIdListItem, depth2, null));
							System.out.println("rootdd id : "+ gOINodeContainer.rootId + " item id: "  +  FreshIdListItem +" depth "+ depth2);
							});
							//depth ++;
						}
						
						GOINodeContainerList.add(gOINodeContainer);
					}
					
					for (int x =0; x< GOINodeContainerList.size() ; x++ ){
						
						for (int y =0; y< GOINodeContainerList.size() ; y++ ){
							
							if(GOINodeContainerList.get(x) != GOINodeContainerList.get(y)){
								Object[] keyArray = GOINodeContainerList.get(x).GOINodeObjectList.keySet().toArray();
								for (int  z=0; z<keyArray.length ; z++  )	{
									Object key = keyArray[z];
								//	System.out.println("keyy "+ key);
									GOINodeObject gobj = GOINodeContainerList.get(y).GOINodeObjectList.get(key);
									
									if(gobj != null){
										
									//	System.out.println("gobj not null "+ gobj.nodeId +" total: "+ (gobj.dist +GOINodeContainerList.get(x).GOINodeObjectList.get(key).dist)  );	
										if(gobj.dist +GOINodeContainerList.get(x).GOINodeObjectList.get(key).dist <= limita ){
										
										System.out.println("root "+ GOINodeContainerList.get(x).rootId +" keey "+ gobj.nodeId +" gobj added dist2: "+ gobj.dist+ " total: "+ (gobj.dist +GOINodeContainerList.get(x).GOINodeObjectList.get(key).dist)  );		
											GOINodeContainerList.get(x).finalIds.add((String) key);	
											
											
											
											if(GOINodeContainerList.get(x).listOfpathslist.get(key)   != null)		
											GOINodeContainerList.get(x).pathList.addAll(GOINodeContainerList.get(x).listOfpathslist.get(key));
										}
										
									}							
									
								}		
									
							}
							
						}
					}
					System.out.println("eee2");
				/*	
					GOINodeContainerList.forEach(gcii -> {
						gcii.finalIds.forEach(fid -> {
							
							System.out.println("root: " + gcii.rootId +  " fid "+fid);
						});
						
						
					});*/
	/*
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


			
	/*
				finalNodes.addAll(streamObjList.get(0).nodesList.values());

				streamObjList.forEach(streamItem -> {

					finalNodes.retainAll(streamItem.nodesList.values());

					//System.out.println(streamItem.nodesList.values());
					//System.out.println("eee");
				});

				*/
				
					System.out.println("eee");
				GOINodeContainerList.forEach(gci -> {
					Set<Relationship> allRelationsgci = new HashSet<Relationship>(); 
					Set<Node> nodes = new HashSet<Node>(); 
					gci.pathList.forEach(pathitem -> {
						  
						  if (pathitem!= null && pathitem.relationships() != null) {
						 
						  pathitem.relationships().forEach(relitem -> { if (relitem !=
						 null) allRelationsgci.add( relitem); });
						 
						 } 
						  
						  if (pathitem!= null && pathitem.nodes() != null) {
								 
							  pathitem.nodes().forEach(nodeitem -> {
								  if (nodeitem != null && gci.finalIds.contains(nodeitem.getProperty("id")) )
								  { 
									  System.out.println("node final listede "+ nodeitem.getProperty("id") + " "+ nodeitem.getProperty("label"));
									  nodes.add( nodeitem); 
									  
								  }
							  });
							 
							 } 					  
					}
					
						 
						 );
								
					
				/*	String queryToPurify1 = "match (a) where a.id in {SourceidList} return collect(a) as nodes";

					Map<String, Object> parametersPurify1 = new HashMap<String, Object>();

					// association id verildi
					
					
									
					parametersPurify1.put("SourceidList", gci.finalIds.add(gci.rootId));
					//parametersPurify1.put("finalNodes", gci.finalIds);
					//parametersPurify1.put("relas", allRelationsgci);
					//parametersPurify.put("nodas", gci.finalIds.add(gci.rootId));
					
					Result resultPurify1 = graphDb.execute(queryToPurify1, parametersPurify1);
					
					// association dönen uygun level1 pathları
					// ve
					// idleri result3 ile dönecek
					while (resultPurify1.hasNext()) {
						Map<String, Object> rowPurify = resultPurify1.next();

						// FreshIdListToIterate.addAll((List<Object>)
						// row3.get("idList"));
						if((List<Node>) rowPurify.get("nodes") != null)
						nodes.addAll((List<Node>) rowPurify.get("nodes"));
				//	System.out.println("nodesListPurify "+(List<Node>) rowPurify.get("nodes"));
						////System.out.println("relsListPurify "+ (List<Relationship>) rowPurify.get("rels"));
						
						
						
					}	
					*/
					///node forecah get  relations YAP
					
					
					String queryToPurify = "match (a) where a.id = {SourceidList} with a  optional match (b)  where  b.id in {finalNodes} with a, b optional match  p= (a)-[r*]-(b) where  all(rv IN  nodes(p)  WHERE rv in {nodas} or rv.id in {SourceidList})   return relationships(p) as rels, nodes(p) as nodes" ;
					

					Map<String, Object> parametersPurify = new HashMap<String, Object>();

					// association id verildi
					
					System.out.println("id "+gci.rootId);
					System.out.println("fnodes "+ gci.finalIds);
					System.out.println("all rel "+allRelationsgci);
					System.out.println("nodas "+nodes);
									
					parametersPurify.put("SourceidList", gci.rootId);
					parametersPurify.put("finalNodes", gci.finalIds);
					parametersPurify.put("relas", allRelationsgci);
					parametersPurify.put("nodas", nodes);
					
					nodes.forEach(nit -> {
						
					System.out.println(nit.getProperty("id") +" "+ nit.getProperty("label"));	
						
						
						
					});
					
					
					Result resultPurify = graphDb.execute(queryToPurify, parametersPurify);
					
					// association dönen uygun level1 pathları
					// ve
					// idleri result3 ile dönecek
					while (resultPurify.hasNext()) {
						Map<String, Object> rowPurify = resultPurify.next();

						// FreshIdListToIterate.addAll((List<Object>)
						// row3.get("idList"));
						if((List<Node>) rowPurify.get("nodes") != null)
						nodesListPurify.addAll((List<Node>) rowPurify.get("nodes"));
				//	System.out.println("nodesListPurify "+(List<Node>) rowPurify.get("nodes"));
						////System.out.println("relsListPurify "+ (List<Relationship>) rowPurify.get("rels"));
						
						if((List<Relationship>) rowPurify.get("rels") !=null)
					  relsListPurify.addAll((List<Relationship>) rowPurify.get("rels"));
						
					}			
					
				});
				
				

		
				
				String queryF = "Match (g)  where  not g:Port and g in {nodes} optional match (g)<-[:resideIn]-(gc) optional match (g)-[:hasPort]->(p:Port) optional match (g)-[:resideIn]->(prnt) optional match (sblg) - [:resideIn] -> (prnt) where sblg <> g return g, collect(gc) as childGlyph, collect(p) as ports, collect(distinct prnt) as prntList, collect(sblg) as sblgList, count(prnt) as  prntcount  order by prntcount desc";

				Map<String, Object> parametersF = new HashMap<String, Object>();
				parametersF.put("nodes", nodesListPurify);
				
				Result resultF = graphDb.execute(queryF, parametersF);

				while (resultF.hasNext()) {
					Map<String, Object> row = resultF.next();

					List<Node> ports = (List<Node>) row.get("ports");
					List<Node> childGlyphs = (List<Node>) row.get("childGlyph");

					Glyph glyph;
					String glyphId = (String) ((Node) row.get("g")).getProperty("id");
					// ////System.out.println(glyphId);
					if (glist.containsKey(glyphId))
						glyph = glist.get(glyphId);
					else { // ////System.out.println("gg " + glyphId);
						glyph = new Glyph();
					}

					glyph.setId(glyphId);
					//// //System.out.println("dddddddddddddddddddddddd "+ ((Node)
					//// row.get("g")).getLabels().iterator().next().toString());
					//// //System.out.println("dddddddddddddddddddddddd "+ ((Node)
					//// row.get("g")).getLabels().iterator().next().toString().replaceAll("_",
					//// " "));

					glyph.setClazz(((Node) row.get("g")).getLabels().iterator().next().toString().replaceAll("_", " "));

					if (!((String) ((Node) row.get("g")).getProperty("compRef")).equals("notexist")) {
						Glyph glyphforCompartment = new Glyph();
						comprefList.add((String) ((Node) row.get("g")).getProperty("compRef"));

						//// //System.out.println((String) ((Node)
						//// row.get("g")).getProperty("compRef"));
						glyphforCompartment.setId((String) ((Node) row.get("g")).getProperty("compRef"));
						// buraya ekleme yapılacak comprefler çekilecek
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
							// ////System.out.println("gc " + childglyphId);
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
							//// //System.out.println("aa " + (String)
							//// item.getProperty("compRef"));
							comprefList.add((String) item.getProperty("compRef"));
							Glyph cglyphforCompartment = new Glyph();
							cglyphforCompartment.setId((String) item.getProperty("compRef"));
							childglyph.setCompartmentRef(cglyphforCompartment);

						}

						glist.putIfAbsent(childglyph.getId(), childglyph);
						//// //System.out.println("ch "+ childglyph.getId());
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
						// ////System.out.println(glyphId);
						if (glist.containsKey(glyphParentId))
							glyphParent = glist.get(glyphParentId);
						else { // ////System.out.println("gg " + glyphId);
							glyphParent = new Glyph();

							glyphParent.setId(glyphParentId);
							//// //System.out.println("22222
							//// "+parent.getLabels().iterator().next().toString());
							//// //System.out.println("wwwwww "+
							//// parent.getLabels().iterator().next().toString().replaceAll("_",
							//// " "));

							glyphParent.setClazz(parent.getLabels().iterator().next().toString().replaceAll("_", " "));

							if (!((String) parent.getProperty("compRef")).equals("notexist")) {
								Glyph glyphforCompartmentp = new Glyph();
								comprefList.add((String) parent.getProperty("compRef"));
								//// //System.out.println((String)
								//// parent.getProperty("compRef"));
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
									// ////System.out.println("gc " + childglyphId);
								}
								childglyph.setId(childglyphId);

								//// //System.out.println("olsun
								//// artııııııııııııııııııııııııııııııııııııııııııııııııııııııııııııık
								//// "+item.getLabels().iterator().next().toString().toString().replaceAll("_",
								//// " "));;
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
									//// //System.out.println("aa " + (String)
									//// item.getProperty("compRef"));
									comprefList.add((String) item.getProperty("compRef"));
									Glyph cglyphforCompartment = new Glyph();
									cglyphforCompartment.setId((String) item.getProperty("compRef"));
									childglyph.setCompartmentRef(cglyphforCompartment);

								}

								glist.putIfAbsent(childglyph.getId(), childglyph);
								//// //System.out.println("ch " + childglyph.getId());
								set.add(childglyph.getId());
								glyphParent.getGlyph().add(childglyph);

							});
						}
						glist.putIfAbsent(glyphParent.getId(), glyphParent);
						// ////System.out.println("gg "+ glyph.getId());
						/// glyph.setId( );
					}

				}

			
				
				// burada cmprefler in comparmentlar çekielcek
				String queryComprefs = "Match (g)  where g.id in {comprefIds} return g";

				Map<String, Object> parametersComprefs = new HashMap<String, Object>();
				parametersComprefs.put("comprefIds", comprefList.toArray());
				Result resultCompRef = graphDb.execute(queryComprefs, parametersComprefs);

				while (resultCompRef.hasNext()) {
					Map<String, Object> row = resultCompRef.next();

					Glyph glyph;
					String glyphId = (String) ((Node) row.get("g")).getProperty("id");
					// //System.out.println(glyphId);
					if (glist.containsKey(glyphId))
						glyph = glist.get(glyphId);
					else { // //System.out.println("gg " + glyphId);
						glyph = new Glyph();
					}

					glyph.setId(glyphId);
					// //System.out.println("dddddddddddddddddddddddd "+ ((Node)
					// row.get("g")).getLabels().iterator().next().toString());
					// //System.out.println("dddddddddddddddddddddddd "+ ((Node)
					// row.get("g")).getLabels().iterator().next().toString().replaceAll("_",
					// " "));

					glyph.setClazz(((Node) row.get("g")).getLabels().iterator().next().toString().replaceAll("_", " "));

					if (!((String) ((Node) row.get("g")).getProperty("compRef")).equals("notexist")) {
						Glyph glyphforCompartment = new Glyph();
						comprefList.add((String) ((Node) row.get("g")).getProperty("compRef"));
						// //System.out.println((String) ((Node)
						// row.get("g")).getProperty("compRef"));
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
				
				// glist.values().;

				// Result result = graphDb.execute( "MATCH (a)-[r]->(b) where
				// r.startX is not null return r as arc") ;
				// "match (a:macromolecule {id: 'glyph19'})-[:traverseEdge*2]-(b) with
				// a, collect (b) as endList match p = shortestPath((a)-[r*]-(c)) where
				// NONE( rel in relationships(p) WHERE type(rel)='traverseEdge') and c
				// in endList with relationships(p) as rels MATCH (a)-[r]->(b) where
				// r.startX is not null and r in rels return a.id as sid,b.id as tid,
				// r.startX as xx,r.startY as yy, r.endX as ex, r.endY as ey, r.aid as
				// id, type(r) as class");
				
				
				
				String query = "MATCH (a)-[r]->(b) where r.startX is not null  and r in {rels} return a.id as sid,b.id as tid, r.startX as xx,r.startY as yy, r.endX as ex, r.endY as ey, r.aid as id, type(r) as class";

				Map<String, Object> parameters1 = new HashMap<String, Object>();

			    parameters1.put("rels", relsListPurify);

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

					//// //System.out.println( cnts+" VAL: "+value.getId());
					cnts++;
				}
				Sbgn sss = new Sbgn();
				sss.setMap(sbgnMap);
				String enes = writeToString(sss);
				return enes;
				} catch (Exception e) {
					// Mark Transaction as failed
					if (tx != null) {
						tx.failure();
					}
					e.printStackTrace();
				} finally {
					// Close Transaction
					if (tx != null) {
						tx.close();
					}
				}
				return "mustafa";
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
			//////// //System.out.println(gid);
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
			//////// //System.out.println(" ss " + g.getClazz());
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

			// InsertBox(g.getBbox(),g);

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
			//////// //System.out.println(gid);
			float y = g.getY();
			float x = g.getX();

			// float w = g.getW();
			// float h = g.getH();
			//////// //System.out.println(" ss " + g.getClazz());
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
				// ////////System.out.println(glyphId);
				if (glist.containsKey(glyphId))
					glyph = glist.get(glyphId);
				else { // ////////System.out.println("gg " + glyphId);
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
			
			Result result2 = db.execute(
					"MATCH (a)-[r]->(b) where r.startX is not null return a.id as sid,b.id as tid, r.startX as xx,r.startY as yy, r.endX as ex, r.endY as ey, r.aid as id, type(r) as class");
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
			String enes = writeToString1(sss);

			return enes;
		}

		private void AddToFreshPidTupleList(HashMap<String, FreshIdItem> freshPidTupleList, Collection<Object> list, Object pid) {
			
			
			list.forEach(iditem ->{
				if(iditem != null && iditem != pid){
					String k = iditem.toString()+pid;
					freshPidTupleList.put(k, new FreshIdItem(iditem, pid));	
				}
				
				
			});
			// TODO Auto-generated method stub
			
		}

		private void GoiNodePathExtract(GOINodeContainer gOINodeContainer, List<String> list, List<Path> list2, double depth) {
			list.forEach(FreshIdListItem ->{
			gOINodeContainer.listOfpathslist.putIfAbsent((String) FreshIdListItem, list2);
				
						
				//System.out.println("rooooot id : "+ gOINodeContainer.rootId + " item id: "  +  FreshIdListItem +" depth "+ depth2);
		});
			
		}
}