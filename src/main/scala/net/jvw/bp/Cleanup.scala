package net.jvw.bp;

import java.io._
import scala.io.Source
import scala.collection.mutable._

object Cleanup {


def main(args: Array[String]) {
	if (args.length<5) {
		println("geef 5 argumenten mee, 1 is bronbestand, 2 is bestandsnaam voor de output, 3 is minPts, 4 is redRadius en 5 is yellowRadius van refPoint")
		return
	}

	val inputFileName=args(0)
	val outputFileName=args(1)
	val fw=new FileWriter(outputFileName)
	val minPts=args(2).toInt //minimum points within radius to be a core point
	val redRadius=args(3).toDouble
	val yellowRadius=args(4).toDouble
	
	val all=CsvUtil.collectFromCsv(inputFileName)
	//tweede arraybuffer bevat een lijst van id's van punten die verbonden zijn met GpsPoint
	val outputPoints=new ArrayBuffer[(GpsPoint, ArrayBuffer[Int])]
	val endPoints=new ArrayBuffer[(GpsPoint, ArrayBuffer[Int])]
	val uncheckedPoints=new ArrayBuffer[GpsPoint] // nog niet onderzochte punten
	
	//ingang amelisweerd, beginknooppunt
	// val refPoint=new GpsPoint("ref",0,5.151468,52.069380)
	//amelisweerd
	val refPoint=new GpsPoint("ref", 0, 5.165031, 52.070274)
	
	val refRadius=0.023
	val filtered=all
						.filter(_._1.isInRadius(refPoint, refRadius))
						.filter(_._2>=minPts)
						
	val filtered2=all
								.filter(_._1.isInRadius(refPoint, refRadius))
								.filter(_._2>=minPts)		
	val map=Map.empty[Int, (GpsPoint, ArrayBuffer[Int])] //id, (gpspoint, connection-id)
	
	println("all:"  + all.size)
	println("filtered:" + filtered.size)
	
	//add p to nieuwe lijst
	uncheckedPoints + refPoint
	val inRedRadius=(filtered.filter(_._1.isInRadius(refPoint, redRadius)))
	inRedRadius.foreach { t=>
		filtered - t
	}

	var connections=new ArrayBuffer[Int]	
	var id=10000
	//hier zijn de elementen in de rode al gewist, hoeft dus geen rekening meer mee gehouden te worden
	while (uncheckedPoints.size>0){
		val point=uncheckedPoints(0)
		val inYellowRadius=(filtered.filter(_._1.isInRadius(point, yellowRadius)))
		
		if (inYellowRadius.size>0) {
			id = id+1
			val (p1,i1,r1)=inYellowRadius.sortWith(_._2 > _._2)(0)
			
			//middelpunt tussen point en p1 berekenen 
			val avgPoint=new GpsPoint("avg",id, (point.getLonX()+p1.getLonX())/2,(point.getLatY()+p1.getLatY())/2)
			
			//zoek in de buurt van het middelpunt exclusief de halve rode radius van point
			//dit is nodig om de lijn wat vloeiender te laten lopen (zie voorbeeld op atletiekbaan)
			val avgRadius=(filtered2.filter(!_._1.isInRadius(point, redRadius/2)).filter(_._1.isInRadius(avgPoint, 0.00005)))
			
			val avgCorePoint=if (avgRadius.size>0){ avgRadius.sortWith(_._2 > _._2)(0)._1 } else { avgPoint }
			uncheckedPoints + avgCorePoint
			connections+(avgCorePoint.getId)
			
			// println ("distance: " + avgCorePoint.distanceTo(point))
			val inRedRadius1=(filtered.filter(_._1.isInRadius(avgCorePoint, redRadius)))
			inRedRadius1.foreach {
				filtered - _
			}
		} else {
			uncheckedPoints - point
			outputPoints + ((point, connections))
			connections=new ArrayBuffer[Int]
		}
	
	}
	
	//schrijf csv-bestand
	fw.write ("routeId,id,x,y, soort\n")
	outputPoints.foreach { p=>
		
		var connectionsString = "" 
		p._2.foreach { s=> connectionsString=connectionsString+";"+s }
		connectionsString=connectionsString+" "
		
		fw.write(p._1.getCsvString() + ", " + p._1.getId + ">" + connectionsString.substring(1).trim() + "\n")
		map+=(p._1.getId -> p)
	}
	
	//vind eindpunten
	outputPoints.foreach { t=>
		val (pt, con)=t
		if (con.size==0){
			endPoints + t
		}
	}
	
	println ("endpoints found: " + endPoints.size)
	
	endPoints.foreach { t=>
		val id=t._1.getId
		val enl=outputPoints.filter(_._2.contains(id))
			
		if (enl.size==1){
			val laatstePunt=t._1
			val eenNaLaatstePunt=enl(0)._1
			
			val inYellowRadius=(filtered2.filter(_._1.isInRadius(laatstePunt, yellowRadius))
													.filter(!_._1.isInRadius(eenNaLaatstePunt, yellowRadius))
			)
			//sorteren
			if (inYellowRadius.size>0){
				val nieuwPunt=inYellowRadius.sortWith(_._2 > _._2)(0)
				fw.write(nieuwPunt._1.getCsvString() + ", open" + laatstePunt.getId + "\n")

				val nearest=outputPoints
										.filter(_._1.distanceTo(nieuwPunt._1)<yellowRadius)
										.filter(_._1.getId!=laatstePunt.getId)
										.sortWith(_._1.distanceTo(nieuwPunt._1) < _._1.distanceTo(nieuwPunt._1))
				if (nearest.size>0){
					outputPoints + ((nieuwPunt._1, ArrayBuffer(laatstePunt.getId, nearest(0)._1.getId)))
					println ("adding outputpoint: " + ArrayBuffer(laatstePunt.getId, nearest(0)._1.getId))
				}
				println ("id: " + laatstePunt.getId + ", enl-id: " + eenNaLaatstePunt.getId)
			}
		}
			
		//TODO let op dat er geen dubbele paden gemaakt gaan worden
		1
	}

	fw.close()

	createAndWriteGpx(outputFileName, outputPoints, map)
	
	println("filtered:" + filtered.size)
	println("outputPoints:" + outputPoints.size)
	println ("uncheckedPoints:" + uncheckedPoints.size)
}

def createAndWriteGpx(outputFileName:String, outputPoints:ArrayBuffer[(GpsPoint, ArrayBuffer[Int])], map:Map[Int, (GpsPoint, ArrayBuffer[Int])]):Unit = {
	val gw=new FileWriter(outputFileName+".gpx")
	
	gw.write ("<?xml version=\"1.0\" encoding=\"UTF-8\"?><gpx creator=\"\" version=\"1.1\" xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 gpx.xsd \">\n")
	
	//waypoints
	outputPoints.foreach {  t=>
		val (pt, con)=t
		
		if (con.size!=1) {
			// println ("pt: "  + pt)
			gw.write ("  <wpt lat=\"" + pt.getLatY + "\" lon=\"" + pt.getLonX + "\" />\n")
		}
	}
	
	//routes
	outputPoints.foreach {  t=>
		val (pt, con)=t
		
		if (con.size!=1) {
			
			con.foreach { cx=>
				gw.write("  <rte>\n")
				gw.write ("    <rtept lat=\"" + pt.getLatY + "\" lon=\"" + pt.getLonX + "\" />\n")
				addConnectionRecursive(cx, gw, map)
				gw.write("  </rte>\n")
			}
		}	
		
	}
	
	gw.write ("</gpx>")
	gw.close()
}

//map = id, (gpspoint, connection-id)
def addConnectionRecursive(id:Int, gw:FileWriter, map:Map[Int, (GpsPoint, ArrayBuffer[Int])]):Unit = {
	
	if (map.contains(id)){
		val (point, connections)=map(id)
		gw.write ("    <rtept lat=\"" + point.getLatY + "\" lon=\"" + point.getLonX + "\" />\n")
		if (connections.size!=1){
			//eindpunt of nieuw knooppunt, doe niets
		} else {
			addConnectionRecursive(connections(0), gw, map)
		}
	} else {
		println ("id: " + id + " not found in map!")
	}
}


}