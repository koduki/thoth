package cn.orz.pascal.thoth
import com.google.appengine.api.datastore.Text
import cn.orz.pascal.gae.persist.DataStore._
import cn.orz.pascal.gae.persist.DataStore
import java.net.URL
import java.io.InputStreamReader
import scala.xml.XML

object Feed{
  
  def apply(link:String) = {
    val xml = XML.load(new InputStreamReader((new URL(link)).openStream()));    
    val item = (xml \\ "channel")(0)
    val feed = Entity("feed") += ( ('title -> (item \ ("title") text) )
		       ,('link -> (item \ ("link") text) )
		       ,('id -> item.attributes.value.toString)
		       ,('description -> (item \ ("description") text)))
    if( !DataStore.exist('feed, 'id, feed('id))){
      println("step1")
      DataStore.put(feed)
    }
    new Feed(feed)
  }
}

class Feed(val src:Entity2, var entries:List[Entry]) {
  def this(src:Entity2) = this(src, List())
  
  def sync() = {
    val xml = XML.load(new InputStreamReader((new URL( src('id) ).openStream)));
    (for(item <- xml \\ "item") yield {
      Entity("entry", src.key) += (('title -> (item \ ("title") text) )
					,('link -> (item \ ("link") text) )
					,('id -> item.attributes.value.toString)
					,('published -> java.util.Calendar.getInstance.getTime())
					,('updated -> (item \ ("date") text) )
					,('category -> "")
					,('summary -> new Text(item \ ("description") text)))
    }).filter(x => !DataStore.exist('entry, 'id, x('id))).
       foreach(x => DataStore.put(x))

    this
  }
}
