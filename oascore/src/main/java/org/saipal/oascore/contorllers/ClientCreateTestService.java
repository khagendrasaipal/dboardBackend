package org.saipal.oascore.contorllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.saipal.fmisutil.service.AutoService;
import org.saipal.fmisutil.util.HttpRequest;
import org.saipal.fmisutil.util.Messenger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ClientCreateTestService extends AutoService{

	public String store() {
		
		ClientCreateTest model = new ClientCreateTest();
		model.loadData(document);
		
		String baseUrl = "http://localhost:8011";
		String client_id=model.client_id;
		String client_name=model.client_name;
		String redirect_uri=model.redirect_uri;
		String client_secret=model.client_secret;
		String status=model.status;
		String urlpart="?client_id="+client_id+"&redirect_uri="+redirect_uri+"&client_name="+client_name+"&client_secret="+client_secret+"&status="+status;
		String url = baseUrl + "/add-clients"+urlpart;
		HttpRequest requests = new HttpRequest();
		JSONObject responses = requests.get(url);
		System.out.println(responses);
		return responses.toString();
	}

	public List<Tuple> getOrgs(HttpServletRequest request) {
		String sql = "select * from organization where id="+request("orgid");
		List<String> argList = new ArrayList<String>();
		List<Tuple> tList = db.getResultList(sql, argList);
		return tList;
		
	}

	public ResponseEntity<Map<String, Object>> getProgram() {
		String sql = "select id,title from programme where 1=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(1));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("id"));
				mapadmlvl.put("name", t.get("title"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getIndicators(String id) {
		String sql = "select uid,data_elements,data_elements_np from data_elements where pid=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(id));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("uid"));
				mapadmlvl.put("name", t.get("data_elements"));
				mapadmlvl.put("namenp", t.get("data_elements_np"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getData(HttpServletRequest request) throws Exception {
		String indId=request("iid");
		String fy=request("fy");
		String ouid="YqQbkwADI71";
		String pe=preparePeriods(fy);
		String pef = pe.replaceFirst(".$","");
//		System.out.println(pef);
		  String link = "/api/analytics.json?dimension=dx:"+indId+"&dimension=pe:"+pef+"&filter=ou:"+ouid;
//		  String link = "/api/29/analytics?dimension=dx:"+indId+",ou:"+ouid+"&filter=pe:"+pe;
		  System.out.println(getResponse(link));
		  return Messenger.getMessenger().setData(getResponse(link)).success();
//		return null;
	}

	private String preparePeriods(String fys) {
		String pe = "";
		int i;
        int fy=Integer.parseInt(fys) ;
            int nextFy = fy+1;
            for(i=12;i>0;i--){
                if(i>9){
                    pe +=fy+""+i+";";
                }else{
                    if(i<4){
                        pe +=nextFy+"0"+i+";";
                    }else{
                        pe +=fy+"0"+i+";";
                    }
                }
            }
        // }
//        pe = rtrim(pe,";");
        return pe;
	}

	private List<String> getResponse(String link) throws Exception {
		String user = "opml.dashboard";
		String pass= "#Mis@2020";
		String server = "http://hmis.gov.np/hmis";
		String uri = link;
		String credentials = "Basic "+Base64.getEncoder().encodeToString((user+":"+pass).getBytes());
		String url=server+uri;
		HttpRequest requests = new HttpRequest();
		JSONObject responses = requests.setHeader("Authorization",credentials)
				.setHeader("Accept", "application/json").get(url);
		JSONObject resp = responses.getJSONObject("data");

		JSONArray datas = resp.getJSONArray("rows");
		Map<Integer,String> mdata = new HashMap<>();
//		System.out.println(datas);
		for (int i = 0 ; i < datas.length(); i++) {
			JSONArray obj = datas.getJSONArray(i);
			int month = Integer.parseInt(obj.getString(1).substring(4));
			mdata.put(month, obj.getString(2));
		}
		Map<Integer,String> months = new LinkedHashMap<>();
		months.put(4,"Shrawn");
		months.put(5,"Bhadra");
		months.put(6,"Ashoj");
		months.put(7,"Kartik");
		months.put(8,"Mangsir");
		months.put(9,"Poush");
		months.put(10,"Magh");
		months.put(11,"Falgun");
		months.put(12,"Chaitra");
		months.put(1,"Baishakh");
		months.put(2,"Jestha");
		months.put(3,"Ashar");
		//("4","Shrawn","5","Bhadra","6","Ashoj","7","Kartik","8","Mangsir","9","Poush","10","Magh","11","Falgun","12","Chaitra","1","Baishakh","2","Jestha","3","Ashar");
		List<Integer> mindex = Arrays.asList(4,5,6,7,8,9,10,11,12,1,2,3);
		List<String> fdata = new ArrayList<>();
		List<String> mnths = new ArrayList<>();
		for(int m:mindex) {
			if(mdata.containsKey(m)) {
				fdata.add(mdata.get(m));
			}else {
				fdata.add("0");
			}
			
			mnths.add(months.get(m));
		}
//		System.out.println(fdata);
		return fdata;

	}
}
