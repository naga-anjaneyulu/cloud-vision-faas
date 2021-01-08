package app;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.FaceAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageSource;
import com.google.cloud.vision.v1.LocationInfo;

public class Vision implements HttpFunction {

	public void service(HttpRequest request, HttpResponse response) throws Exception {
		 response.appendHeader("Access-Control-Allow-Origin", "*");
		 if ("OPTIONS".equals(request.getMethod())) {
			 System.out.print("hey");
		      response.appendHeader("Access-Control-Allow-Methods", "GET");
		      response.appendHeader("Access-Control-Allow-Headers", "Content-Type");
		      response.appendHeader("Access-Control-Max-Age", "3600");
		      response.setStatusCode(HttpURLConnection.HTTP_NO_CONTENT);  }
       System.out.println("Started Processing request");
       Map<String, String> reqmap = processRequest(request);
        String req_url = reqmap.get("url");
		String operation = reqmap.get("op");
        if(req_url != null) {
        	req_url = req_url.trim();
            try { URL url = new URL(req_url);
             InputStream in = new BufferedInputStream(url.openStream());
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             byte[] buf = new byte[1024];
             int n = 0;
             while (-1!=(n=in.read(buf)))
             {
                out.write(buf, 0, n);
             }
             out.close();
             in.close();
             byte[] images = out.toByteArray();
             String path = System.getProperty("user.dir");
             Storage storage = StorageOptions.getDefaultInstance().getService();
             Bucket bucket = storage.get("naga-bucket2");
             Blob blob = bucket.create("image.jpg", images);
             
             String url2 = "gs://naga-bucket2/image.jpg";
             
             String result = "";
             if(operation.equals("df")) {
            	result = detectFacesGcs(url2);
             }else {
            	 result = detectLandmarksGcs(url2);
             }
            	
                BufferedWriter out1 = response.getWriter();
                out1.write(result);
                System.out.println("Finished Processing request");
            } catch (IOException e) {
                e.printStackTrace();
            }
	}
	}
	
	public Map<String, String> processRequest(HttpRequest request) {
  	  System.out.println("Started Building requestObj");
 		 Map<String, String> mappedObj = new HashMap<String, String>();
 	        try {
 	            mapObject(mappedObj, request.getReader().lines().collect(Collectors.joining()));
 	        } catch (IOException e) {
 	            e.printStackTrace();
 	        }
 	     System.out.println(mappedObj.toString());
 	     System.out.println("Finished Building requestObj");
 	        return mappedObj;
 		
 	}
    
    private void mapObject(Map<String, String> mappedObj, String body) {
 
		try {
            JSONObject jsonData = (JSONObject) new JSONParser().parse(body);
           
            for(Object key: jsonData.keySet()) {
                if(key.toString().equals("data")) {
              	  JSONObject urlData = (JSONObject) new JSONParser().parse(jsonData.get(key).toString());
         
              	  
              	  for(Object key1: urlData.keySet()) {
              		  System.out.println(key1.toString());
              		  mappedObj.put(key1.toString(), urlData.get(key1).toString());
              	  }
              	  
              	 
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
		
	}
    
    
    // Detects faces in the specified remote image on Google Cloud Storage.
    public static String detectFacesGcs(String gcsPath) throws IOException {
    	System.out.println("Enter Face detection");
      List<AnnotateImageRequest> requests = new ArrayList<AnnotateImageRequest>();
      StringBuilder sb = new StringBuilder();
      ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
      Image img = Image.newBuilder().setSource(imgSource).build();
      Feature feat = Feature.newBuilder().setType(Feature.Type.FACE_DETECTION).build();
     
      AnnotateImageRequest request =
          AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
      requests.add(request);
      try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
    		System.out.println(" Face detection request sent");
        BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
        List<AnnotateImageResponse> responses = response.getResponsesList();
        System.out.println(" Face detection response recieved");
        for (AnnotateImageResponse res : responses) {
          if (res.hasError()) {
            System.out.format("Error: %s%n", res.getError().getMessage());
            return "";
          }
          sb.append("{");
          for (FaceAnnotation annotation : res.getFaceAnnotationsList()) {
        	  sb.append("\"anger\":");sb.append('"');
        	  sb.append(annotation.getAngerLikelihood().toString());
        	  sb.append('"');sb.append(",");sb.append("\"joy\":"); sb.append('"');
        	  sb.append( annotation.getJoyLikelihood().toString());
        	  sb.append('"'); sb.append(",");sb.append("\"suprise\":");sb.append('"');
        	  sb.append(annotation.getSurpriseLikelihood().toString());
        	  sb.append('"');
          }
          sb.append("}");}}return sb.toString();}

    // Detects landmarks in the specified remote image on Google Cloud Storage.
    public static String detectLandmarksGcs(String gcsPath) throws IOException {
      List<AnnotateImageRequest> requests = new ArrayList<>();
      ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
      Image img = Image.newBuilder().setSource(imgSource).build();
      Feature feat = Feature.newBuilder().setType(Feature.Type.LANDMARK_DETECTION).build();
      AnnotateImageRequest request =
          AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
      requests.add(request);
      StringBuilder sb = new StringBuilder();
      try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
        BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
        List<AnnotateImageResponse> responses = response.getResponsesList();

        for (AnnotateImageResponse res : responses) {
          if (res.hasError()) {
            System.out.format("Error: %s%n", res.getError().getMessage());
            return "";
          }
          sb.append("{");
          for (EntityAnnotation annotation : res.getLandmarkAnnotationsList()) {
            LocationInfo info = annotation.getLocationsList().listIterator().next();
            sb.append("\"LandMark\":");
            sb.append('"');
            sb.append( annotation.getDescription().toString());
            sb.append('"');
            break;
          }
          sb.append("}");
        }
      }
      
      return sb.toString();
    }



} 
