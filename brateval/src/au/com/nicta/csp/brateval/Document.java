package au.com.nicta.csp.brateval;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Document class, it includes implementations for entity and relation comparisons 
 * 
 * @author Antonio Jimeno Yepes (antonio.jimeno@gmail.com)
 *
 */
public class Document
{
  private Map <String, Entity> entities = new HashMap <String, Entity> ();
  private Map <String, Relation> relations = new HashMap <String, Relation> ();
  private Map <String, Event> events = new HashMap <String, Event> ();
  private List <Equivalent> equivalents = new LinkedList <Equivalent> ();
  private Map <String, Attribute> attributes = new HashMap <String, Attribute> ();
  private Map <String, Normalization> normalizations = new HashMap <String, Normalization> ();
  private Map <String, Note> notes = new HashMap <String, Note> ();
  private Map <String, Relation> hoprelations = new HashMap <String, Relation> ();

  public void addEntity(String id, Entity entity)
  { entities.put(id, entity); }
  
  public Entity getEntity(String id)
  { return entities.get(id); }

  public void removeEntity(String id)
  { entities.remove(id); }

  public Collection <Entity> getEntities()
  { return entities.values(); }
  
  public void addRelation(String id, Relation relation)
  { relations.put(id, relation); }
  
  public void removeRelation(String id)
  { relations.remove(id); }

  public Relation getRelation(String id)
  { return relations.get(id); }

  public Collection <Relation> getRelations()
  { return relations.values(); }
  
  // add hop
  
  public void addHopRelation(String id, Relation relation)
  { hoprelations.put(id, relation); }
  
  public void removeHopRelation(String id)
  {  hoprelations.remove(id); }

  public Relation getHopRelation(String id)
  { return  hoprelations.get(id); }

  public Collection <Relation> getHopRelations()
  { return  hoprelations.values(); }
  
  //done add hop
  
  public void addEvent(String id, Event event)
  { events.put(id, event); }
  
  public void removeEvent(String id)
  { events.remove(id); }
  
  public Event getEvent(String id)
  { return events.get(id); }

  public Collection <Event> getEvents()
  { return events.values(); }
  
  public void addEquivalent(Equivalent equivalent)
  { equivalents.add(equivalent); }

  public Collection <Equivalent> getEquivalents()
  { return equivalents; }
  
  public void addAttribute(String id, Attribute attribute)
  { attributes.put(id, attribute); }
  
  public void removeAttribute(String id)
  { attributes.remove(id); }
  
  public Attribute getAttribute(String id)
  { return attributes.get(id); }

  public Collection <Attribute> getAttributes()
  { return attributes.values(); }

  public void addNormalization(String id, Normalization normalization)
  { normalizations.put(id, normalization); }
  
  public void removeNormalization(String id)
  { normalizations.remove(id); }
  
  public Normalization getNormalization(String id)
  { return normalizations.get(id); }

  public Collection <Normalization> getNormalizations()
  { return normalizations.values(); }

  public void addNote(String id, Note note)
  { notes.put(id, note); }
  
  public void removeNote(String id)
  { notes.remove(id); }
  
  public Note getNote(String id)
  { return notes.get(id); }

  public Collection <Note> getNotes()
  { return notes.values(); }

  public Collection <Entity> getEntitiesByType(String type)
  {
	Collection <Entity> subentities = new ArrayList <Entity> ();

	for (Entity e : entities.values())
	{
	  if (e.getType().equals(type))
	  { subentities.add(e); }
	}

	return subentities;
  }
  
  public Entity findEntityOverlap(Entity e)
  {
    for (Entity e1 : entities.values())
    {
      if (Entity.entityComparisonOverlap(e, e1))
      { return e1; }
    }

	return null;
  }

  public Entity findEntityOverlapNoType(Entity e)
  {
    for (Entity e1 : entities.values())
    {
      if (Entity.entityComparisonSpanOverlap(e, e1))
      { return e1; }
    }

	return null;
  }
  
  public Entity findEntitySpanOverlap(Entity e)
  {
    for (Entity e1 : entities.values())
    {
      if (Entity.entityComparisonSpanOverlap(e, e1) && !e.equals(e1))
      { return e1; }
    }

	return null;
  }

  public boolean hasEntitySpanOverlapNC(Entity e)
  {
    for (Entity e1 : entities.values())
    {
      if (Entity.entityComparisonSpanOverlap(e, e1) && !e.equals(e1))
      { return true; }
    }

	return false;
  }

  public List <Entity> findAllEntitiesSpanOverlap(Entity e)
  {
	List <Entity> entities_span = new ArrayList <Entity> ();

    for (Entity e1 : entities.values())
    {
      if (Entity.entityComparisonSpanOverlap(e, e1) || Entity.entityComparisonSpanOverlap(e1, e))
      {
        if (!e.equals(e1))
        { entities_span.add(e1); }
      }
    }

	return entities_span;
  }
  
  public Entity findEntity(Entity e)
  {
    for (Entity e1 : entities.values())
    {
      if (Entity.entityComparison(e, e1) && !e.equals(e1))
      { return e1; }
    }

	return null;
  }

  public Entity findEntityOverlapC(Entity e)
  {
    for (Entity e1 : entities.values())
    {
      if (e1.getType().equals(e.getType()) && Entity.entityComparisonSpanOverlap(e, e1) && !e.equals(e1))
      { return e1; }
    }

	return null;
  }
  
  public Entity findEntitySimilarString(Entity e, double min_similarity) {
    Entity rc = null;
    for (Entity e1 : entities.values())
    {
      if (e1.getType().equals(e.getType()) && !e.equals(e1))
      {
      	double sim = Entity.entityComparisonStringSimilarity(e, e1, min_similarity);
      	if (sim >= min_similarity)
      	{
	      	min_similarity = sim;
	      	rc = e1;
      	}
  	  }
    }

	return rc;
  }
  /**
   * Find relation in a given document
   * 
   * @param relation
   * @param d
   * @return Return the relation in the matching document or null if no relation can be found
   */
  public Relation findRelationOverlap(Relation relation)
  {
    for (Relation rd : getRelations())
    {
      // Compare relation type
      if (relation.getRelationType().equals(rd.getRelationType())
      && (    		 
          Entity.entityComparisonOverlap(relation.getEntity1(), rd.getEntity1())
      &&  Entity.entityComparisonOverlap(relation.getEntity2(), rd.getEntity2())
    	 )
      )
      {
        // Compare entities
        return rd;
      }
    }

	return null;
  }

  /**
   * Find relation in a given document
   * 
   * @param relation
   * @param d
   * @return Return the relation in the matching document or null if no relation can be found
   */
  public Relation findRelation(Relation relation)
  {
    for (Relation rd : getRelations())
    {
      // Compare relation type
      if (relation.getRelationType().equals(rd.getRelationType())
      && ((
          Entity.entityComparison(relation.getEntity1(), rd.getEntity1())
      &&  Entity.entityComparison(relation.getEntity2(), rd.getEntity2())
    	 )
      // Order does not matter
      || (
           Entity.entityComparison(relation.getEntity1(), rd.getEntity2())
       &&  Entity.entityComparison(relation.getEntity2(), rd.getEntity1())
       	 )
      )
      )
      {
        // Compare entities
        return rd;
      }
    }

	return null;  
  }

  // Look for entity relation
  public boolean hasEntityRelation(Entity e)
  {
    for (Relation r : getRelations())
    {
      if (Entity.entityComparison(e, r.getEntity1()) || Entity.entityComparison(e, r.getEntity2()))
      { return true; }
    }

    return false;
  }



// revise down there

  public Relation findRelationOverlapHop(Relation relation)
  {/*
    for (ArrayList<Entity> rd : getHopRelations(relation)) // here relation is two entity and no relation type already
//    for (Relation rd : getRelations())
    {
      // Compare relation type
     // if (Entity.entityComparisonOverlapNotype(relation.getEntity1(), rd.get(0))
    //  &&  Entity.entityComparisonOverlapNotype(relation.getEntity2(), rd.get(1))
      if (Entity.entityComparisonOverlap(relation.getEntity1(), rd.get(0))
      &&  Entity.entityComparisonOverlap(relation.getEntity2(), rd.get(1))
    	 )
      {
        // Compare entities
        // should return rd but it is the same?
        return relation;
      }
    }*/
    for (Relation rd : getHopRelations())
    {
      // Compare relation type
      if (relation.getRelationType().equals(rd.getRelationType())
      && (    		 
          Entity.entityComparisonOverlap(relation.getEntity1(), rd.getEntity1())
      &&  Entity.entityComparisonOverlap(relation.getEntity2(), rd.getEntity2())
    	 )
      )
      {
        // Compare entities
        return rd;
      }
    }

	  return null;
  }
  
  
  public Relation findRelationHop(Relation relation)
  { /*
    for (ArrayList<Entity> rd : getHopRelations(relation)) // here relation is two entity and no relation type already
 //   for (Relation rd : getRelations())
    {
      // Compare relation type
  //    if (Entity.entityComparisonNotype(relation.getEntity1(), rd.get(0))
  //    &&  Entity.entityComparisonNotype(relation.getEntity2(), rd.get(1))
      if (Entity.entityComparison(relation.getEntity1(), rd.get(0))
      &&  Entity.entityComparison(relation.getEntity2(), rd.get(1))
    	 )
      {
        // Compare entities
        // should return rd but it is the same? 
        return relation;
      }
    }
  */
    for (Relation rd : getHopRelations())
      {
        // Compare relation type
        if (relation.getRelationType().equals(rd.getRelationType())
        && ((
            Entity.entityComparison(relation.getEntity1(), rd.getEntity1())
        &&  Entity.entityComparison(relation.getEntity2(), rd.getEntity2())
      	 )
        /*// Order does not matter
        || (
             Entity.entityComparison(relation.getEntity1(), rd.getEntity2())
         &&  Entity.entityComparison(relation.getEntity2(), rd.getEntity1())
         	 )
        */
        )
        )
        {
          // Compare entities
          return rd;
        }
      }
  	return null;  
  }
  /*
  public static boolean entityComparisonDuplicate(Entity e1, Entity e2)
  {
    if ((Entity.entityComparison(e1, rd.getEntity1())
     &&  Entity.entityComparison(comparedEntity.getEntity2(), rd.getEntity2())
  	 )
     // Order does not matter
     || (
         Entity.entityComparison(comparedEntity.getEntity1(), rd.getEntity2())
     &&  Entity.entityComparison(comparedEntity.getEntity2(), rd.getEntity1())
         )
     )
    {
        return true;
      }
    else
    {return false;}
      
  }
  */
  public static boolean entityCheckMiddle(Entity entity, ArrayList<Entity> cluster)
  {
    //check whether in middle or not. not begining and not end.
    int clustersize =  cluster.size();
    for (int i = 1; i< clustersize-1; i++)
    //for (Entity e : cluster)
    {
      if (Entity.entityComparisonNotype(cluster.get(i), entity))
      {
        return true;
      }
    }
    return false;
  }
  public static boolean equalcluser(ArrayList<Entity> tempCluster, ArrayList<Entity> cluster)
  {
    if (tempCluster.size() != cluster.size())
    {return false;}
    
    for (int clusterindex = 0; clusterindex < cluster.size(); clusterindex++)
    {
      if (!Entity.entityComparisonNotype(tempCluster.get(clusterindex), cluster.get(clusterindex)))
      {return false;}
    }
    return true;
  }
  
  
  public static boolean inClusters(ArrayList<Entity> tempCluster, ArrayList<ArrayList<Entity>> clusters)
  {
    //check whether tempCluster in clusters
    
    for (ArrayList<Entity> cluster : clusters)
    //for (Entity e : cluster)
    {
      if (equalcluser(tempCluster, cluster))
      {
        return true;
      }
    }
    return false;
  }
  
  public ArrayList<ArrayList<Entity>> getRelationClusters(String relationtype)
  {
    ArrayList<ArrayList<Entity>> clusters = new ArrayList<ArrayList<Entity>>();
    for (Relation rd : getRelations())
    {
      // fist of all, make sure it is the right relations
      if (relationtype.equals(rd.getRelationType()))
      {
 //       System.out.println("per relationship:");
 //       System.out.println(rd.getRelationType());
        
        boolean findCluster = false;
        int clustersCount = clusters.size();
 //       System.out.println("per clustersCount:");
//        System.out.println(clustersCount);
//        System.out.println(clusters);
        for (int clusterIndex = 0; clusterIndex < clustersCount; clusterIndex++) 
        { 
          if (Entity.entityComparisonNotype(rd.getEntity1(), clusters.get(clusterIndex).get(clusters.get(clusterIndex).size() - 1))) // pair [c,d] and cluster [a,b,c]   get [a, b, c, d]
          { 
            
            ArrayList<Entity> tempCluster = clusters.get(clusterIndex);
            tempCluster.add(rd.getEntity2());
            findCluster = true;
   //         System.out.println("case 1");
          }
          else if (Entity.entityComparisonNotype(rd.getEntity2(), clusters.get(clusterIndex).get(0)))  //  pair [d,a] and cluster [a,b,c]  get [d, a, b, c]
          {
            ArrayList<Entity> tempCluster = new ArrayList<Entity>();
            tempCluster.add(rd.getEntity1());
            for (Entity e : clusters.get(clusterIndex))
            {
              tempCluster.add(e);
            }
            clusters.set(clusterIndex, tempCluster);
            findCluster = true;   
    //        System.out.println("case 2");
          }
          else if (entityCheckMiddle(rd.getEntity1(), clusters.get(clusterIndex)))  // pair [b,d] and cluster [a,b,c]   in the middle  get [a,b,c] and [a,b,d] - not the begining or end
          {
            ArrayList<Entity> tempCluster = new ArrayList<Entity>();
            for (Entity e : clusters.get(clusterIndex))
            {
              if (!Entity.entityComparisonNotype(e, rd.getEntity1()))
              {
                tempCluster.add(e);
              }
              else
              {break;}
            }
            tempCluster.add(rd.getEntity1());
            tempCluster.add(rd.getEntity2());
            if (!inClusters(tempCluster, clusters))  // remove redundancy 
            {clusters.add(tempCluster); }
            findCluster = true;
    //        System.out.println("case 3");
          }
          else if (entityCheckMiddle(rd.getEntity2(), clusters.get(clusterIndex))) // pair [d,b] and cluster [a,b,c]   in the middle get [a,b,c] and [d,b,c]  - not the begining or end
          { 
            ArrayList<Entity> tempCluster = new ArrayList<Entity>();
            tempCluster.add(rd.getEntity1());
            tempCluster.add(rd.getEntity2());
            boolean startInsert = false;
            for (Entity e : clusters.get(clusterIndex))
            {
              if (startInsert)
              {tempCluster.add(e);}
                   
              if (Entity.entityComparisonNotype(e, rd.getEntity2()))
              {startInsert = true;}
            }
            if (!inClusters(tempCluster, clusters)) // remove redundancy 
            {clusters.add(tempCluster); }
            
            findCluster = true;
            
  //          System.out.println("\ncase 4");
  //          System.out.println(rd.getFile());
  //          System.out.println(rd.getEntity1());
   //         System.out.println(rd.getEntity2());
  //          System.out.println(clusterIndex);
   //         System.out.println(clusters.get(clusterIndex));
  //          System.out.println(tempCluster);
            
          }
        }
    //    System.out.println(findCluster);
        if (!findCluster)
        {
          ArrayList<Entity> tempCluster = new ArrayList<Entity>();
          tempCluster.add(rd.getEntity1());
          tempCluster.add(rd.getEntity2());
          clusters.add(tempCluster);
//          System.out.println("case 5");
        }
  //      System.out.println("per findCluster:");
  //      System.out.println(findCluster);
      }
    }
    return clusters;
  }
  
  public void handleHopRelationsPer(String relationtype, String file)
  { 
 //   System.out.println("\nbefore clustering:");
    
    ArrayList<ArrayList<Entity>> clusters = getRelationClusters(relationtype);
 //   System.out.println("\nafter clustering:");
  //  System.out.println("Entity");
  //  System.out.println(getEntities());
  //  System.out.println("per cluster:");
 //   System.out.println(clusters);
    Set<ArrayList<Entity>> hopRelations = new HashSet<ArrayList<Entity>>();
    
    for (ArrayList<Entity> cluster : clusters)
    {
      int clusterCount = cluster.size();
      for (int i = 0; i < clusterCount; i++) 
      {
        for (int j = i+1; j < clusterCount; j++) 
        {
          ArrayList<Entity> tempRelation = new ArrayList<Entity>();
          tempRelation.add(cluster.get(i));
          tempRelation.add(cluster.get(j));
          hopRelations.add(tempRelation);
        }
      }
    } 
//    System.out.println("\nhopRelations");
//    System.out.println(hopRelations.size());
    
    // get the set relation and now add them into the hop for the particular type
   // System.out.println("\nrelationtype");
  //  System.out.println(relationtype);
    String [] relationtypeline = relationtype.split("\\|");
  //  System.out.println("relationtypeline");
  //  System.out.println(relationtypeline);
  //  System.out.println(relationtypeline[0]);
  //  System.out.println(relationtypeline[1]);
  //  System.out.println(relationtypeline[2]);
    // Work-up|work-up|entity
    for (ArrayList<Entity> re : hopRelations)
    {
      // construct the entity first
     // Entity anaphora = (Entity) re.get(0).clone(); // use a new one!
     // Entity antecedent = (Entity) re.get(1).clone(); //
      // new Entity(id, type, l, string, file)
    //  System.out.println("\nanaphora");
    //  System.out.println(re.get(0));
   //   System.out.println(re.get(0).getId());
      
  //    System.out.println(re.get(0).getType());
  //    System.out.println(re.get(0).getLocations());
      
  //    System.out.println(re.get(0).getString());
  //    System.out.println(re.get(0).getFile());
      
      Entity anaphora = new Entity(re.get(0).getId(), re.get(0).getType(), re.get(0).getLocations(), re.get(0).getString(), re.get(0).getFile());
      Entity antecedent = new Entity(re.get(1).getId(), re.get(1).getType(), re.get(1).getLocations(), re.get(1).getString(), re.get(1).getFile());
  //    System.out.println(anaphora);
  //    System.out.println(antecedent);
  //    System.out.println(anaphora, antecedent);
      //need to reset the entity type
      anaphora.setType(relationtypeline[1]);
      antecedent.setType(relationtypeline[2]);
      // if not find entity set id and add it to the entity list 
      /*
      boolean findanaphora = false;
      boolean findantecedent = false;
      for (Entity e : getEntities())
      {
        if (Entity.entityComparison(anaphora, e))
        {findanaphora = true;}
        if (Entity.entityComparison(antecedent, e))
        {findantecedent = true;}
      }
      */
      if (findEntity(anaphora)==null) // new entity and add it to entity list
      {
        String id_str = String.valueOf(entities.size()+ 1); 
        String id = "T" + id_str;
        anaphora.setId(id);
        addEntity(id, anaphora);
      }
      if (findEntity(antecedent)==null) // new entity and add it to entity list
      {
        String id_str = String.valueOf(entities.size()+ 1); 
        String id = "T" + id_str;
        antecedent.setId(id);
        addEntity(id, antecedent);
      }
      
      // now construct the reation
      String id_str = String.valueOf(hoprelations.size()+ 1); 
      String id = "R" + id_str;
      addHopRelation(id, new Relation(id, relationtypeline[0], "Arg1", anaphora, "Arg2", antecedent, file)); 
    }
  }

  public void handleHopRelations(String file)
  { 
  //  System.out.println(file);
    Set<String> relationtypes = new HashSet<String>();
    for (Relation r : getRelations())
    { relationtypes.add(r.getRelationType()); }
  // System.out.println("\n\nrelationtypes");
  //  System.out.println(relationtypes);
    for (Iterator<String> it = relationtypes.iterator(); it.hasNext(); ) 
    //for (String relationtype : relationtypes.)
    { 
      String relationtype = it.next();
   //   System.out.println("\nrelationtype");
  //    System.out.println(relationtype);
      if (relationtype.contains("Coreference"))                // only consider coref here
      {handleHopRelationsPer(relationtype, file);}
    //  handleHopRelationsPer(relationtype, file);
 //     System.out.println("hoprelations.size()");
 //     System.out.println(hoprelations.size());
    }
   // System.out.println("\nhoprelations");
   // System.out.println(getHopRelations());
   // System.out.println("relations");
   // System.out.println(getRelations());
   
//    for (Relation r : getHopRelations())
//    {
//      printRelation(r);
//    }
//    for (Relation r : getRelations())
 //   {
//      printRelation(r);
//    }
  //  System.out.println("getRelations().size()");
 //   System.out.println(getRelations().size());
//    System.out.println("getHopRelations().size()");
//    System.out.println(getHopRelations().size());
//    System.out.println("\n");
  }
  public void printRelation(Relation r)
  {
    System.out.println(r.getRelationType());
    Entity e1 = r.getEntity1();
    System.out.println("\nanaphora");
    System.out.println(e1);
    Entity e2 = r.getEntity2();
    System.out.println("antecedent");
    System.out.println(e2);
  }
} 