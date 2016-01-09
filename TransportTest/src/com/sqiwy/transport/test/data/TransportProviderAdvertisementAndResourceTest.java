///**
// * Created by abrysov
// */
//package com.sqiwy.transport.test.data;
//
//import java.util.Arrays;
//import java.util.UUID;
//
//import android.test.ProviderTestCase2;
//import android.test.mock.MockContentResolver;
//
//import com.sqiwy.transport.advertisement.Advertisement;
//import com.sqiwy.transport.advertisement.Res;
//import com.sqiwy.transport.data.Point;
//import com.sqiwy.transport.data.TransportProvider;
//import com.sqiwy.transport.data.TransportProviderHelper;
//
///**
// * @author r_karpiak
// *
// */
//public class TransportProviderAdvertisementAndResourceTest extends ProviderTestCase2<TransportProvider> {
//
//	/**
//	 * @param providerClass
//	 * @param providerAuthority
//	 */
//	public TransportProviderAdvertisementAndResourceTest() {
//		super(TransportProvider.class, "com.sqiwy.transport.test.data.transportprovider");
//	}
//
//	public void testAdInsertQuery() {
//		MockContentResolver resolver = getMockContentResolver();
//
//		for (int i = 0; i < 10; i++) {
//			String guid = UUID.randomUUID().toString();
//
//			Advertisement adToInsert = new Advertisement();
//
//			adToInsert.setGuid(guid);
//			Res res = new Res();
//			res.setGuid("res_" + i);
//			adToInsert.setResource(res);
//			adToInsert.setShowDuration(1000 + i * 1000);
//			adToInsert.setTrigger(String.valueOf(i));
//			adToInsert.setType("type_" + i);
//			adToInsert.setStatus("ready");
//			adToInsert.setVersion(i);
//
//			Point point = new Point();
//			point.setLatitude(12);
//			point.setLongitude(13);
//			point.setRadius(1);
//			point.setName("test");
//
//			if (i % 2 == 0) {
//				adToInsert.setPoints(Arrays.asList(generatePoint(i, i + 10, i + 100)));
//			}
//
//			TransportProviderHelper.insertAd(resolver, adToInsert);
//
//			Advertisement adQueried = TransportProviderHelper.queryAd(resolver, adToInsert.getId());
//
//			assertFalse(adToInsert == adQueried);
//			assertEquals(adToInsert, adQueried);
//		}
//	}
//
//	public void testAdInsertQueryByGuid() {
//		MockContentResolver resolver = getMockContentResolver();
//
//		for (int i = 0; i < 10; i++) {
//			String guid = UUID.randomUUID().toString();
//
//			Advertisement adToInsert = new Advertisement();
//
//			adToInsert.setGuid(guid);
//			Res res = new Res();
//			res.setGuid("res_" + i);
//			adToInsert.setResource(res);
//			adToInsert.setShowDuration(1000 + i * 1000);
//			adToInsert.setTrigger(String.valueOf(i));
//			adToInsert.setType("type_" + i);
//			adToInsert.setVersion(i);
//			adToInsert.setStatus("inactive");
//
//			TransportProviderHelper.insertAd(resolver, adToInsert);
//
//			Advertisement adQueried = TransportProviderHelper.queryAd(resolver, guid);
//
//			assertFalse(adToInsert == adQueried);
//			assertEquals(adToInsert, adQueried);
//		}
//	}
//
//	public void testAdInsertOverrideQueryByGuid() {
//		MockContentResolver resolver = getMockContentResolver();
//
//		String guid = UUID.randomUUID().toString();
//
//		for (int i = 0; i < 10; i++) {
//
//			Advertisement adToInsert = new Advertisement();
//
//			adToInsert.setGuid(guid);
//			Res res = new Res();
//			res.setGuid("res_" + i);
//			adToInsert.setResource(res);
//			adToInsert.setShowDuration(1000 + i * 1000);
//			adToInsert.setTrigger(String.valueOf(i));
//			adToInsert.setType("type_" + i);
//			adToInsert.setVersion(i);
//			adToInsert.setStatus("another");
//
//			if (i % 2 == 0) {
//				adToInsert.setPoints(Arrays.asList(generatePoint(i, i + 3, i + 3), generatePoint(i, i + 10, i + 100), generatePoint()));
//			}
//
//			TransportProviderHelper.insertAd(resolver, adToInsert);
//
//			Advertisement adQueried = TransportProviderHelper.queryAd(resolver, guid);
//
//			assertFalse(adToInsert == adQueried);
//			assertEquals(adToInsert, adQueried);
//		}
//	}
//
//	protected Point generatePoint() {
//		return generatePoint(12, 13, 1);
//	}
//	protected Point generatePoint(double lat, double lon, float radius) {
//		Point point = new Point();
//		point.setLatitude(lat);
//		point.setLongitude(lon);
//		point.setRadius(radius);
//		point.setName("test");
//		return point;
//	}
//
//	public void testResInsertQuery() {
//		MockContentResolver resolver = getMockContentResolver();
//
//		for (int i = 0; i < 10; i++) {
//			String guid = UUID.randomUUID().toString();
//
//			Res resToInsert = new Res();
//			resToInsert.setAccessUri("access" + i);
//			resToInsert.setArchive(0 == i % 2);
//			resToInsert.setBytes(1000 + i * 1000);
//			resToInsert.setGuid(guid);
//			resToInsert.setStorageUri("storage" + i);
//			resToInsert.setUri("uri" + i);
//			resToInsert.setVersion(i);
//
//			TransportProviderHelper.insertRes(resolver, resToInsert);
//
//			Res resQueried = TransportProviderHelper.queryRes(resolver, resToInsert.getId());
//
//			assertFalse(resToInsert == resQueried);
//			assertEquals(resToInsert, resQueried);
//		}
//	}
//
//	public void testResInsertQueryByGuid() {
//		MockContentResolver resolver = getMockContentResolver();
//
//		for (int i = 0; i < 10; i++) {
//			String guid = UUID.randomUUID().toString();
//
//			Res resToInsert = new Res();
//			resToInsert.setAccessUri("access" + i);
//			resToInsert.setArchive(0 == i % 2);
//			resToInsert.setBytes(1000 + i * 1000);
//			resToInsert.setGuid(guid);
//			resToInsert.setStorageUri("storage" + i);
//			resToInsert.setUri("uri" + i);
//			resToInsert.setVersion(i);
//
//			TransportProviderHelper.insertRes(resolver, resToInsert);
//
//			Res resQueried = TransportProviderHelper.queryRes(resolver, guid);
//
//			assertFalse(resToInsert == resQueried);
//			assertEquals(resToInsert, resQueried);
//		}
//	}
//
//	public void testResInsertOverrideQueryByGuid() {
//		MockContentResolver resolver = getMockContentResolver();
//
//		String guid = UUID.randomUUID().toString();
//
//		for (int i = 0; i < 10; i++) {
//			Res resToInsert = new Res();
//			resToInsert.setAccessUri("access" + i);
//			resToInsert.setArchive(0 == i % 2);
//			resToInsert.setBytes(1000 + i * 1000);
//			resToInsert.setGuid(guid);
//			resToInsert.setStorageUri("storage" + i);
//			resToInsert.setUri("uri" + i);
//			resToInsert.setVersion(i);
//
//			TransportProviderHelper.insertRes(resolver, resToInsert);
//
//			Res resQueried = TransportProviderHelper.queryRes(resolver, guid);
//
//			assertFalse(resToInsert == resQueried);
//			assertEquals(resToInsert, resQueried);
//		}
//	}
//
//}
