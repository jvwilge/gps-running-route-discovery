package net.jvw.bp

import scala.collection.mutable

class GpsPoint(routeId:String, id:Int, lonX:Double, latY:Double, isJunction:Boolean) {
	
	val connectedTo:mutable.Set[GpsPoint]=mutable.Set()
	
	def this(routeId:String, id:Int, lonX:Double, latY:Double) {
		this(routeId, id, lonX, latY, false)
	}
	
	def getCsvString():String = {
		routeId + ", " + id + ", " + lonX +", " + latY
	}
	
	def isInRadius(center:GpsPoint, radius:Double ):Boolean = {
		val deltaX=Math.abs(center.getLonX-this.getLonX)
		if (deltaX>radius) {
			return false
		}
  
		val deltaY=Math.abs(center.getLatY-this.getLatY)
		if (deltaY>radius) {
			 return false
		}
  
		val deltaXY=Math.sqrt(Math.pow(deltaX, 2)+Math.pow(deltaY,2))
		if (deltaXY>radius){ 
		    return false
		}
		true
	}
 
	//Distance to other point
	def distanceTo(other:GpsPoint):Double = {
	  val deltaX=other.getLonX-this.getLonX
	  val deltaY=other.getLatY-this.getLatY
	  Math.sqrt(Math.pow(deltaX, 2)+Math.pow(deltaY,2))
	}
	
	def getLonX(): Double = lonX
	def getLatY(): Double = latY
	def getId(): Int = id
	
	def getIsJunction():Boolean =  (isJunction || connectedTo.size>2 )
	
	def addConnection(gpsPoint:GpsPoint):Unit = {
		connectedTo + gpsPoint
	}
	
	override def toString = "[routeId: " + routeId + ", id: " + id + ", lonX: "+ lonX + ", latY: " + latY + ", isJunction: " + isJunction + ", connections: " +  connectedTo.size  + "]"
}
