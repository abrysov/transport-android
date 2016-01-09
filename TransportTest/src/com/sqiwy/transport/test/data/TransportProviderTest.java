/**
 * Created by abrysov
 */
package com.sqiwy.transport.test.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.database.Cursor;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.sqiwy.transport.data.Point;
import com.sqiwy.transport.data.Route;
import com.sqiwy.transport.data.TransportProvider;
import com.sqiwy.transport.data.TransportProvider.Table;
import com.sqiwy.transport.data.TransportProviderHelper;
import com.sqiwy.transport.data.Vehicle;

/**
 * @author r_karpiak
 *
 */
public class TransportProviderTest extends ProviderTestCase2<TransportProvider> {

	/**
	 * @param providerClass
	 * @param providerAuthority
	 */
	public TransportProviderTest() {
		super(TransportProvider.class, "com.sqiwy.transport.test.data.transportprovider");
	}
	
	public void testVehicleInsertQuery() {
		MockContentResolver resolver = getMockContentResolver();
		
		for (int i = 0; i < 10; i++) {
			String guid = UUID.randomUUID().toString();
			
			Vehicle vehicleTo = new Vehicle();
			vehicleTo.setDescription("Test description");
			vehicleTo.setName("A035");
			vehicleTo.setGuid(guid);
			
			TransportProviderHelper.insertVehicle(resolver, vehicleTo);
			
			Vehicle vehicleFrom = TransportProviderHelper.queryVehicle(resolver, vehicleTo.getId());
			
			assertFalse(vehicleTo == vehicleFrom);
			assertEquals(vehicleTo, vehicleFrom);
		}
	}
	
	public void testVehicleWithRoutesInsertQuery() {
		MockContentResolver resolver = getMockContentResolver();
		
		for (int i = 0; i < 10; i++) {
			String guid = UUID.randomUUID().toString();
			
			Vehicle vehicleTo = new Vehicle();
			vehicleTo.setDescription("Test description");
			vehicleTo.setName("A035");
			vehicleTo.setGuid(guid);
			
			List<Route> routes = new ArrayList<Route>();
			for (int j = 0; j < 3; j++) {
				Route route = new Route();
				route.setDuration(1000);
				route.setStart(new Date(System.currentTimeMillis() - (i + 1) * 1000));
				route.setEnd(new Date());
				route.setDirection("DIRECTION" + i);
				
				routes.add(route);
			}
			vehicleTo.setRoutes(routes);
			
			TransportProviderHelper.insertVehicle(resolver, vehicleTo);
			
			Vehicle vehicleFrom = TransportProviderHelper.queryVehicle(resolver, vehicleTo.getId());
			
			assertFalse(vehicleTo == vehicleFrom);
			assertEquals(vehicleTo, vehicleFrom);
		}
	}
	
	public void testVehicleWithRoutesAndPointsInsertQuery() {
		MockContentResolver resolver = getMockContentResolver();
		
		for (int i = 0; i < 10; i++) {
			String guid = UUID.randomUUID().toString();
			
			Vehicle vehicleTo = new Vehicle();
			vehicleTo.setDescription("Test description");
			vehicleTo.setName("A035");
			vehicleTo.setGuid(guid);
			
			List<Route> routes = new ArrayList<Route>();
			for (int j = 0; j < 3; j++) {
				Route route = new Route();
				route.setDuration(1000);
				route.setStart(new Date(System.currentTimeMillis() - (i + 1) * 1000));
				route.setEnd(new Date());
				route.setDirection("DIRECTION" + i);
				
				List<Point> points = new ArrayList<Point>();
				for (int k = 0; k < 5; k++) {
					Point point = new Point();
					point.setLatitude(12.234234 + 5.55 * k);
					point.setLongitude(54.532342 + 7.77 * k);
					point.setOrder(k);
					point.setArrivalTime(new Date());
					points.add(point);
				}
				route.setPoints(points);
				
				routes.add(route);
			}
			vehicleTo.setRoutes(routes);
			
			TransportProviderHelper.insertVehicle(resolver, vehicleTo);
			
			Vehicle vehicleFrom = TransportProviderHelper.queryVehicle(resolver, vehicleTo.getId());
			
			assertFalse(vehicleTo == vehicleFrom);
			assertEquals(vehicleTo, vehicleFrom);
		}
	}
	
	public void testVehicleDelete() {
		MockContentResolver resolver = getMockContentResolver();
		
		for (int i = 0; i < 10; i++) {
			String guid = UUID.randomUUID().toString();
			
			Vehicle vehicleTo = new Vehicle();
			vehicleTo.setDescription("Test description");
			vehicleTo.setName("A035");
			vehicleTo.setGuid(guid);
			
			List<Route> routes = new ArrayList<Route>();
			for (int j = 0; j < 3; j++) {
				Route route = new Route();
				route.setDuration(1000);
				route.setStart(new Date(System.currentTimeMillis() - (i + 1) * 1000));
				route.setEnd(new Date());
				route.setDirection("DIRECTION" + i);
				
				List<Point> points = new ArrayList<Point>();
				for (int k = 0; k < 5; k++) {
					Point point = new Point();
					point.setLatitude(12.234234 + 5.55 * k);
					point.setLongitude(54.532342 + 7.77 * k);
					point.setOrder(k);
					point.setArrivalTime(new Date());
					points.add(point);
				}
				route.setPoints(points);
				
				routes.add(route);
			}
			vehicleTo.setRoutes(routes);
			
			TransportProviderHelper.insertVehicle(resolver, vehicleTo);
			
			Vehicle vehicleFrom = TransportProviderHelper.queryVehicle(resolver, vehicleTo.getId());
			
			assertFalse(vehicleTo == vehicleFrom);
			assertEquals(vehicleTo, vehicleFrom);
			
			TransportProviderHelper.deleteVehicle(resolver, vehicleTo.getId());
			
			vehicleFrom = TransportProviderHelper.queryVehicle(resolver, vehicleTo.getId());
			assertNull(vehicleFrom);
			
			Cursor cursor = resolver.query(Table.Vehicle.URI, null, null, null, null);
			assertTrue(cursor.moveToNext());
			cursor.close();
			
			cursor = resolver.query(Table.Route.URI, null, null, null, null);
			assertTrue(cursor.moveToNext());
			cursor.close();
			
			cursor = resolver.query(Table.RoutePoint.URI, null, null, null, null);
			assertTrue(cursor.moveToNext());
			cursor.close();
		}
	}
	
	public void testRouteInsertQuery() {
		MockContentResolver resolver = getMockContentResolver();
		
		String guid = UUID.randomUUID().toString();
		
		Vehicle vehicle = new Vehicle();
		vehicle.setDescription("Test description");
		vehicle.setName("A035");
		vehicle.setGuid(guid);
		
		TransportProviderHelper.insertVehicle(resolver, vehicle);
		
		Route route = new Route();
		route.setDuration(1000);
		route.setStart(new Date(System.currentTimeMillis() - 1000));
		route.setEnd(new Date());
		route.setDirection("DIRECTION");
		
		List<Route> routes = Arrays.asList(route);
		TransportProviderHelper.insertRoutes(getMockContentResolver(), 123, routes);
		for (Route r : routes) {
			assertEquals(-1, r.getId());
		}
		
		TransportProviderHelper.insertRoutes(getMockContentResolver(), vehicle.getId(), routes);
		
		routes = TransportProviderHelper.queryRoutes(resolver, vehicle.getId());
		
		assertFalse(routes.isEmpty());
		
		assertEquals(1, routes.size());
		assertEquals(route, routes.get(0));
	}
	
	public void testRouteInsertQueryMultiple() {
		MockContentResolver resolver = getMockContentResolver();
		
		String guid = UUID.randomUUID().toString();
		
		Vehicle vehicle = new Vehicle();
		vehicle.setDescription("Test description");
		vehicle.setName("A035");
		vehicle.setGuid(guid);
		
		TransportProviderHelper.insertVehicle(resolver, vehicle);
		
		List<Route> routes = new ArrayList<Route>();
		
		int routesNumber = 10;
		for (int i = 0; i < routesNumber; i++) {
			Route route = new Route();
			route.setDuration(1000);
			route.setStart(new Date(System.currentTimeMillis() - (i + 1) * 1000));
			route.setEnd(new Date());
			route.setDirection("DIRECTION" + i);
			
			routes.add(route);
		}
		
		TransportProviderHelper.insertRoutes(getMockContentResolver(), 123, routes);
		for (Route route : routes) {
			assertEquals(-1, route.getId());
		}
			
		TransportProviderHelper.insertRoutes(getMockContentResolver(), vehicle.getId(), routes);
		
		List<Route> result = TransportProviderHelper.queryRoutes(resolver, vehicle.getId());
		
		assertFalse(result.isEmpty());
		
		assertEquals(routesNumber, result.size());
		for (int i = 0; i < routesNumber; i++) {
			assertEquals(routes.get(i), result.get(i));
		}
	}
	
	public void testPointInsertQuery() {
		MockContentResolver resolver = getMockContentResolver();
		
		String guid = UUID.randomUUID().toString();
		
		Vehicle vehicle = new Vehicle();
		vehicle.setDescription("Test description");
		vehicle.setName("A035");
		vehicle.setGuid(guid);
		
		TransportProviderHelper.insertVehicle(resolver, vehicle);
		
		Route route = new Route();
		route.setDuration(1000);
		route.setStart(new Date(System.currentTimeMillis() - 1000));
		route.setEnd(new Date());
		route.setDirection("DIRECTION");
		
		List<Route> routes = Arrays.asList(route);
		TransportProviderHelper.insertRoutes(getMockContentResolver(), 123, routes);
		for (Route r : routes) {
			assertEquals(-1, r.getId());
		}
		
		TransportProviderHelper.insertRoutes(getMockContentResolver(), vehicle.getId(), routes);

		Point point = new Point();
		point.setLatitude(12.234234);
		point.setLongitude(54.532342);
		point.setOrder(4);
		point.setArrivalTime(new Date());
		
		TransportProviderHelper.insertRoutePoints(resolver, 3454, Arrays.asList(point));
		assertEquals(-1, point.getId()); // Point has not been inserted
		
		TransportProviderHelper.insertRoutePoints(resolver, route.getId(), Arrays.asList(point));
		List<Point> result = TransportProviderHelper.queryRoutePoints(resolver, route.getId());
		
		assertFalse(result.isEmpty());
		assertTrue(1 == result.size());
		assertEquals(point, result.get(0));
	}
	
	public void testPointInsertQueryMultiple() {
		MockContentResolver resolver = getMockContentResolver();
		
		String guid = UUID.randomUUID().toString();
		
		Vehicle vehicle = new Vehicle();
		vehicle.setDescription("Test description");
		vehicle.setName("A035");
		vehicle.setGuid(guid);
		
		TransportProviderHelper.insertVehicle(resolver, vehicle);
		
		Route route = new Route();
		route.setDuration(1000);
		route.setStart(new Date(System.currentTimeMillis() - 1000));
		route.setEnd(new Date());
		route.setDirection("DIRECTION");
		
		List<Route> routes = Arrays.asList(route);
		TransportProviderHelper.insertRoutes(getMockContentResolver(), 123, routes);
		for (Route r : routes) {
			assertEquals(-1, r.getId());
		}
		
		TransportProviderHelper.insertRoutes(getMockContentResolver(), vehicle.getId(), routes);

		List<Point> points = new ArrayList<Point>();
		int pointsNumber = 10;
		for (int i = 0; i < pointsNumber; i++) {
			Point point = new Point();
			point.setLatitude(i * 12.234234);
			point.setLongitude(i * 54.532342);
			point.setOrder(i * 4);
			point.setArrivalTime(new Date(System.currentTimeMillis() + i * 1000));
			
			points.add(point);
		}
		
		
		TransportProviderHelper.insertRoutePoints(resolver, 3454, points);
		for (Point point : points) {
			assertEquals(-1, point.getId()); // Point has not been inserted
		}
		
		TransportProviderHelper.insertRoutePoints(resolver, route.getId(), points);
		List<Point> result = TransportProviderHelper.queryRoutePoints(resolver, route.getId());
		
		assertFalse(result.isEmpty());
		assertTrue(pointsNumber == result.size());
		
		for (int i = 0; i < pointsNumber; i++) {
			assertEquals(points.get(i), result.get(i));
		}
	}

}
