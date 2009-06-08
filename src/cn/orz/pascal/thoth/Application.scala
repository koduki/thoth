package cn.orz.pascal.thoth
import com.google.appengine.api.users.{User, UserService, UserServiceFactory}
import com.google.appengine.api.datastore._

import cn.orz.pascal.gae.persist.DataStore._
import cn.orz.pascal.gae.framework.AbstractRoute  

object Application extends AbstractRoute{  
  get("/"){(req:Request, res:Response) =>  res.redirect( "/index.html" )}
  get("/favicon.ico"){(req:Request, res:Response) =>  <p>a</p>}
  
  get("/index.html"){ (req:Request, res:Response) =>
    val params = req.params
    var entries:List[cn.orz.pascal.thoth.Application.DataStore.Entity2] = List()
    for( feed <-  DataStore from('feed) where('id === params("id")) asList()) yield {
      entries = (for( entry <-  DataStore from('entry) asIterator() if (entry.parent == feed.key)) yield {entry}).toList
    }

	<html lang="ja">
	    <head>
	    	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	    	<title>Feed Reader</title>
	    </head>
	    <body>
		    <h1>Thoth - FeedReader</h1>
		    <p>
		    <form method="POST">
			    link : <input type="text" name="link" /><input type="submit"/>
		    </form>
		    </p>
		    <p>
		       <h2>Feed List.</h2>
		       <ul>
		    	{for( feed <- DataStore from('feed) asIterator) yield  {
		    		<li>{$("<a href='index.html?id="+ feed('id) +"'>"+ feed('title) +"</a>")}</li>
		    	}}
		       </ul>
		    </p>

		    <p>
		       <h2>Entry List.</h2>
		       <ul>
		    	{for( entry <- entries) yield  {
		    		<li>{$("<a href='"+ entry('link) +"'>"+ entry('title) +"</a>")}</li>
		    	}}
		       </ul>
		    </p>  
	    </body>
    </html>
  }  

  post("/index.html"){(req:Request, res:Response) =>
    val params = req.params
    Feed(params("link")).sync
		      
    res.redirect( "/index.html" )
  }

}
