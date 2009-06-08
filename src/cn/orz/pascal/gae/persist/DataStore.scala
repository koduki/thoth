
package cn.orz.pascal.gae.persist
import com.google.appengine.api.datastore._

object DataStore {
	implicit def wrapItt(iterable:java.lang.Iterable[Entity]) = new Iterator[Entity2]{
		val itt = iterable.iterator()
		def hasNext:Boolean = itt.hasNext()
		def next:Entity2 = new Entity2(itt.next())
	}
	    
	implicit def toFilter1(property:Symbol) = new { 
		def ===(value:Any):Query => Query = 
			query => query.addFilter(property.toString, Query.FilterOperator.EQUAL, value) 
		def <=(value:Any):Query => Query = 
			query => query.addFilter(property.toString.drop(1), Query.FilterOperator.LESS_THAN_OR_EQUAL, value) 
		def >=(value:Any):Query => Query = 
			query => query.addFilter(property.toString.drop(1), Query.FilterOperator.GREATER_THAN_OR_EQUAL, value)
		def <(value:Any):Query => Query = 
			query => query.addFilter(property.toString.drop(1), Query.FilterOperator.LESS_THAN, value) 
		def >(value:Any):Query => Query = 
			query => query.addFilter(property.toString.drop(1), Query.FilterOperator.GREATER_THAN, value) 
	}
	def Entity(kind:String) = new Entity2(new Entity(kind))
	def Entity(kind:String, parent:Key) = new Entity2(new Entity(kind, parent))  
	class Entity2(val src:Entity){
	    def key() = src.getKey
	    def parent() = src.getParent
	    def +=(keyName:Symbol, value:Any):Entity2 = {src.setProperty(keyName.toString, value);this}
	    def +=(args:(Symbol, Any)*):Entity2 = {args.foreach(x => this.+=(x._1, x._2)); this}
	    def apply(keyName:Symbol) =  src.getProperty(keyName.toString) match{
			case x:String => x		
			case x:Text => x.getValue
			case _ => ""
		}	   
	} 
	
    val ds = DatastoreServiceFactory.getDatastoreService()
    class Filter(ds:DatastoreService, query:Query){ 
      def asIterator():Iterator[Entity2] = ds.prepare(query).asIterable()
      def asList():List[Entity2] = asIterator.toList      
      def where(predicate:Query => Query) = new FetchOption(ds, predicate(query))
    }
    
    class FetchOption(ds:DatastoreService, query:Query){ 
    	def asIterator():Iterator[Entity2] = ds.prepare(query).asIterable()
    	def asList():List[Entity2] = ds.prepare(query).asIterable.toList
    	def and(predicate:Query => Query) = new FetchOption(ds, predicate(query))
    	def limit(offset:Int, limit:Int) = new {
    	  def asIterator():Iterator[Entity2] = ds.prepare(query).asIterable(FetchOptions.Builder.withLimit(limit).offset(offset)) 
    	} 
     }
    def from(name:Symbol) = new Filter(ds, new Query(name.toString.drop(1)))

  def put(x:Entity2) = {
    DatastoreServiceFactory.getDatastoreService.put(x.src)
  }

  def exist(kind:Symbol, key:Symbol, value:Any) = !(DataStore from(kind) where(key === value) asList() isEmpty)
}
