package net.jvw.bp

import _root_.scala.xml._
import java.io._

object CollectFromXml {
  
	//begin bij hoger getal zodat de avgPoints er nog voor kunnen
	var id=100000;

def main(args: Array[String]) {
	if (args.length<2) {
		println("geef 2 argumenten mee, 1 is brondirectory, 2 is bestandsnaam voor de output")
		return
	}

	val filesLocation=args(0)
	val outputFileName=args(1)
	
	val start=System.nanoTime
	val fw=new FileWriter(outputFileName)
	fw.write ("routeId,id,x,y\n")

	val all=collectFromFiles(filesLocation)
	all.foreach { p:GpsPoint=> fw.write(p.getCsvString() + "\n")}
 
	println (all.length + " points found")
 
 	fw.close()

	val stop=System.nanoTime
	println("time elapsed: " + (stop-start)/1000000  + "ms")
}
 
def collectFromFiles(filesLocation:String):List[GpsPoint] = {
	var all:List[GpsPoint]=Nil
	val x=getTcxFiles(filesLocation)
	x.foreach { 
		fileName:String =>
		val list=collectPoints(filesLocation + "/" + fileName , fileName.split(" ").first )
		all=list:::all
	}
 	all
}
    
def collectPoints(fileLocation:String, routeId:String):List[GpsPoint] = {
	val xml=XML.load(fileLocation)
	val trackpoints = (xml \"Activities"\"Activity").filter( _.attribute("Sport").getOrElse("")=="Running")\"Lap"\"Track"\"Trackpoint"
	var list:List[GpsPoint]=Nil
	
	trackpoints.foreach{
		t:Node => 
		val lonX= xmlToDouble(t \ "Position"\"LongitudeDegrees")
		val latY= xmlToDouble(t \ "Position"\"LatitudeDegrees")
		
		val p=new GpsPoint(routeId , id , lonX ,latY)
		
		if (lonX!=0 || latY!=0)
		    list=p::list
		
		id+=1
	}
	
	val refPoint=new GpsPoint("ref",0,5.151432,52.069355)
	list.filter(_.isInRadius(refPoint, 0.12))
}
    
def getTcxFiles(directoryLocation:String):Array[String] = {
	val d=new File(directoryLocation)
	//TODO nullpointer fixen
	d.list.filter(_.endsWith (".tcx"))
}
    
//TODO zou deze methode echt nodig zijn? :S
def xmlToDouble(n:NodeSeq): Double = {
	val s=n.text
	if (s.length==0){
		0.0
	} else {
		s.toDouble
	}
}

}
