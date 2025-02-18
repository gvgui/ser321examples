/*
Simple Web Server in Java which allows you to call 
localhost:9000/ and show you the root.html webpage from the www/root.html folder
You can also do some other simple GET requests:
1) /random shows you a random picture (well random from the set defined)
2) json shows you the response as JSON for /random instead the html page
3) /file/filename shows you the raw file (not as HTML)
4) /multiply?num1=3&num2=4 multiplies the two inputs and responses with the result
5) /github?query=users/amehlhase316/repos (or other GitHub repo owners) will lead to receiving
   JSON which will for now only be printed in the console. See the todo below

The reading of the request is done "manually", meaning no library that helps making things a 
little easier is used. This is done so you see exactly how to pars the request and 
write a response back
*/

package funHttpServer;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Map;
import java.util.LinkedHashMap;
import java.nio.charset.Charset;
import org.json.JSONArray;
import org.json.JSONObject;

class WebServer {
  public static void main(String args[]) {
    WebServer server = new WebServer(9000);
  }

  /**
   * Main thread
   * @param port to listen on
   */
  public WebServer(int port) {
    ServerSocket server = null;
    Socket sock = null;
    InputStream in = null;
    OutputStream out = null;

    try {
      server = new ServerSocket(port);
      while (true) {
        sock = server.accept();
        out = sock.getOutputStream();
        in = sock.getInputStream();
        byte[] response = createResponse(in);
        out.write(response);
        out.flush();
        in.close();
        out.close();
        sock.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (sock != null) {
        try {
          server.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Used in the "/random" endpoint
   */
  private final static HashMap<String, String> _images = new HashMap<>() {
    {
      put("streets", "https://iili.io/JV1pSV.jpg");
      put("bread", "https://iili.io/Jj9MWG.jpg");
    }
  };

  /**
   * Fruits used for "/fruitOrVeg?"
   */
  private final static HashMap<String, String> red_fruit = new HashMap<>() {
    {
      put("Apple", "https://www.collinsdictionary.com/images/full/apple_158989157.jpg");
      put("Raspberry", "https://cdn.shopify.com/s/files/1/2045/8185/products/3631.jpg");
      put("Strawberry", "https://upload.wikimedia.org/wikipedia/commons/4/4c/Garden_strawberry_%28Fragaria_%C3%97_ananassa%29_single2.jpg");
    }
  };

  private final static HashMap<String, String> orange_fruit = new HashMap<>() {
    {
      put("Orange", "https://i5.peapod.com/c/6H/6HFEA.jpg");
      put("Papaya", "https://solidstarts.com/wp-content/uploads/Papaya-for-Babies-scaled.jpg");
      put("Persimmon", "https://solidstarts.com/wp-content/uploads/Persimmon-for-Babies-scaled.jpg");
    }
  };

  private final static HashMap<String, String> yellow_fruit = new HashMap<>() {
    {
      put("Banana", "https://media.npr.org/assets/img/2011/08/19/istock_000017061174small-6ca3bb7c8b6c768b92153932e822623a95065935.jpg");
      put("Pineapple", "https://www.pamperedchef.com/iceberg/com/product/2416-3-lg.jpg");
      put("Lemon", "https://149359756.v2.pressablecdn.com/wp-content/uploads/2020/05/Lemon.jpg");
    }
  };

  private final static HashMap<String, String> green_fruit = new HashMap<>() {
    {
      put("Kiwi", "https://images.seattletimes.com/wp-content/uploads/2018/01/bafe0de4-f002-11e7-ae5a-0e7cb6fd7c60.jpg");
      put("Pear", "https://www.producemarketguide.com/sites/default/files/Commodities.tar/Commodities/pears_commodity-page.png");
      put("Green Apple", "https://daganghalal.blob.core.windows.net/27005/Product/1000x1000__greenapple1000x1000-1633937107305.png");
    }
  };

  private final static HashMap<String, String> blue_fruit = new HashMap<>() {
    {
      put("Blueberry", "https://solidstarts.com/wp-content/uploads/Blueberries_Edited-1-scaled.jpg");
      put("Blackberry (Close Enough)", "https://health.clevelandclinic.org/wp-content/uploads/sites/3/2022/08/Blackberries-Benefits-121749984-770x533-1.jpg");
    }
  };
  
  private final static HashMap<String, String> purple_fruit = new HashMap<>() {
    {
      put("Grapes", "https://img.freepik.com/premium-vector/isolated-dark-grape-with-green-leaf_317810-1956.jpg");
      put("Plum", "https://saverafresh.com/wp-content/uploads/2021/08/41d0r45jzQL._AC_SS450_.jpg");
    }
  };


  /**
   * Vegetables used for "/fruitOrVeg?"
   */ 
  private final static HashMap<String, String> red_veg = new HashMap() {
    {
      put("Radish", "https://www.highmowingseeds.com/media/catalog/product/cache/6cbdb003cf4aae33b9be8e6a6cf3d7ad/2/8/2850-1.jpg");
      put("Red Bell Pepper", "https://m.media-amazon.com/images/S/assets.wholefoodsmarket.com/PIE/product/5b34f624e54acd7021de4922_redbellpepper-1.jpg");
      put("Chilli Pepper", "https://img.freepik.com/premium-vector/red-hot-chili-pepper-realistic-image_98292-2560.jpg");
    }
  };

  private final static HashMap<String, String> orange_veg = new HashMap() {
    {
      put("Orange Bell Pepper", "https://i5.walmartimages.com/asr/eb1893c6-ff33-4a36-ba05-d7fe42b67fbf.830244cad6037e9c0b18c4c237259b5d.jpeg");
      put("Carrot", "https://bcfresh.ca/wp-content/uploads/2021/11/Carrots.jpg");
      put("Pumpkin", "https://solidstarts.com/wp-content/uploads/Pumpkin-for-Babies-scaled.jpg");
    }
  };

  private final static HashMap<String, String> yellow_veg = new HashMap() {
    {
      put("Yellow Squash", "https://solidstarts.com/wp-content/uploads/Summer-Squash-2-scaled.jpg");
      put("Yellow Bell Pepper", "https://www.producemarketguide.com/sites/default/files/Variety.tar/Variety/yellow-bell-peppers_variety-page.png");
      put("Corn", "https://images.squarespace-cdn.com/content/v1/55674e06e4b0830d6f6d4322/1434732486611-DVLUQVJXKDZ1VX9N4G5C/sweet+corn.jpg");
    }
  };

  private final static HashMap<String, String> green_veg = new HashMap() {
    {
      put("Cabbage", "https://www.gardeningknowhow.com/wp-content/uploads/2020/03/primo-vantage-400x350.jpg");
      put("Broccoli", "https://www.hopkinsmedicine.org/sebin/t/c/broccoli-og.jpg");
      put("Asparagus", "https://cdn.mos.cms.futurecdn.net/xt4UGjZmzih2tt6NS3Awmd.jpg");
    }
  };

  private final static HashMap<String, String> purple_veg = new HashMap() {
    {
      put("Eggplant", "https://assets.bonappetit.com/photos/5f23269615fa96522dcee4ee/5:4/w_2460,h_1968,c_limit/Basically-Eggplant.jpg");
      put("Purple Carrot", "https://www.farmersalmanac.com/wp-content/uploads/2020/11/Purple-carrots-growing-gardening-tips-i121291365.jpeg");
      put("Beets", "https://www.friendsschoolplantsale.com/sites/default/files/images/variety/V-Beets-BullsBlood-BC.jpg");
    }
  };

  private Random random = new Random();

  /**
   * Reads in socket stream and generates a response
   * @param inStream HTTP input stream from socket
   * @return the byte encoded HTTP response
   */
  public byte[] createResponse(InputStream inStream) {

    byte[] response = null;
    BufferedReader in = null;

    try {

      // Read from socket's input stream. Must use an
      // InputStreamReader to bridge from streams to a reader
      in = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));

      // Get header and save the request from the GET line:
      // example GET format: GET /index.html HTTP/1.1

      String request = null;

      boolean done = false;
      while (!done) {
        String line = in.readLine();

        System.out.println("Received: " + line);

        // find end of header("\n\n")
        if (line == null || line.equals(""))
          done = true;
        // parse GET format ("GET <path> HTTP/1.1")
        else if (line.startsWith("GET")) {
          int firstSpace = line.indexOf(" ");
          int secondSpace = line.indexOf(" ", firstSpace + 1);

          // extract the request, basically everything after the GET up to HTTP/1.1
          request = line.substring(firstSpace + 2, secondSpace);
        }

      }
      System.out.println("FINISHED PARSING HEADER\n");

      // Generate an appropriate response to the user
      if (request == null) {
        response = "<html>Illegal request: no GET</html>".getBytes();
      } else {
        // create output buffer
        StringBuilder builder = new StringBuilder();
        // NOTE: output from buffer is at the end

        if (request.length() == 0) {
          // shows the default directory page

          // opens the root.html file
          String page = new String(readFileInBytes(new File("www/root.html")));
          // performs a template replacement in the page
          page = page.replace("${links}", buildFileList());

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append(page);

        } else if (request.equalsIgnoreCase("json")) {
          // shows the JSON of a random image and sets the header name for that image

          // pick a index from the map
          int index = random.nextInt(_images.size());

          // pull out the information
          String header = (String) _images.keySet().toArray()[index];
          String url = _images.get(header);

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: application/json; charset=utf-8\n");
          builder.append("\n");
          builder.append("{");
          builder.append("\"header\":\"").append(header).append("\",");
          builder.append("\"image\":\"").append(url).append("\"");
          builder.append("}");

        } else if (request.equalsIgnoreCase("random")) {
          // opens the random image page

          // open the index.html
          File file = new File("www/index.html");

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append(new String(readFileInBytes(file)));

        } else if (request.contains("file/")) {
          // tries to find the specified file and shows it or shows an error

          // take the path and clean it. try to open the file
          File file = new File(request.replace("file/", ""));

          // Generate response
          if (file.exists()) { // success
            builder.append("HTTP/1.1 200 OK\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append(new String(readFileInBytes(file)));
          } else { // failure
            builder.append("HTTP/1.1 404 Not Found\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("File not found: " + file);
          }
        } else if (request.contains("multiply?")) {
          Map<String, String> query_pairs = new LinkedHashMap<String, String>();
          int test = 0;

          if(request.indexOf("?") > 1) {
            try {
              // extract path parameters
              query_pairs = splitQuery(request.replace("multiply?", ""));
              // extract required fields from parameters
              Integer num1 = Integer.parseInt(query_pairs.get("num1"));
              test++;
              Integer num2 = Integer.parseInt(query_pairs.get("num2"));
              // do math
              Integer result = num1 * num2;
              // Generate response
              builder.append("HTTP/1.1 200 OK\n");
              builder.append("Content-Type: text/html; charset=utf-8\n");
              builder.append("\n");
              builder.append("Result is: " + result);
            }
            catch (NumberFormatException e) {
              builder.append("HTTP/1.1 400 Bad Request\n");
              builder.append("Content-Type: text/html; charset=utf-8\n");
              builder.append("\n");
              if(test == 0) {
                builder.append("Error 400 Bad Request: An integer value must be passed to num1.");
              }
              else if(test == 1) {
                builder.append("Error 400 Bad Request: An integer value must be passed to num2.");
              }
            }
            catch (StringIndexOutOfBoundsException e) {
              builder.append("HTTP/1.1 400 Bad Request\n");
              builder.append("Content-Type: text/html; charset=utf-8\n");
              builder.append("\n");
              builder.append("Error 400 Bad Request: Integer values must be passed to both num1 and num2.");
            }
            catch (Exception e) {
              builder.append("HTTP/1.1 400 Bad Request\n");
              builder.append("Content-Type: text/html; charset=utf-8\n");
              builder.append("\n");
              builder.append("Error 400 Bad Request: Please use the following syntax: multiply?num1=*int*&num2=*int*.");
            }
          }
          else {
            builder.append("HTTP/1.1 400 Bad Request\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Error: An integer value must be passed to num1 and num2");
          }

        } else if (request.contains("github?")) {
          // pulls the query from the request and runs it with GitHub's REST API
          // check out https://docs.github.com/rest/reference/
          //
          // HINT: REST is organized by nesting topics. Figure out the biggest one first,
          //     then drill down to what you care about
          // "Owner's repo is named RepoName. Example: find RepoName's contributors" translates to
          //     "/repos/OWNERNAME/REPONAME/contributors"

          Map<String, String> query_pairs = new LinkedHashMap<String, String>();

          if (request.indexOf("?") > 1) {
            try {
              query_pairs = splitQuery(request.replace("github?", ""));
              String json = fetchURL("https://api.github.com/" + query_pairs.get("query"));
              //System.out.println(json);

              builder.append("HTTP/1.1 200 OK\n");
              builder.append("Content-Type: application/json; charset=utf-8\n");
              builder.append("\n");
              JSONArray arr = new JSONArray(json);
              for(int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                builder.append("Repo Name: " + obj.get("name"));
                builder.append("\n");
                builder.append("ID: " + obj.get("id"));
                builder.append("\n");
                builder.append("Login Name: " + obj.getJSONObject("owner").get("login"));
                builder.append("\n");
                builder.append("\n");
                builder.append("\n");
              }
            }
            catch (StringIndexOutOfBoundsException e) {
              builder.append("HTTP/1.1 400 Bad Request\n");
              builder.append("Content-Type: text/html; charset=utf-8\n");
              builder.append("\n");
              builder.append("400 Error Bad Request: Please use the syntax - /github?query=users/githubusername/repos");
            }
            catch (Exception e) {
              if(!request.contains("query")) {
                builder.append("Error 400 Bad Request: Please use the syntax - /github?query=users/githubusername/repos");
              }
              else if (!request.contains("users")) {
                builder.append("Error 400 Bad Request: Please include the term 'users' in your request. i.e. /github?query=users/.../...");
              }
              else if (!request.contains("repos")) {
                builder.append("Error 400 Bad Request: Please include the term 'repos' in your request. i.e. /github?query=.../.../repos");
              }
              else {
                builder.append("Error 400 Bad Request: Please use a valid github username in your request. i.e. /github?query=.../githubusername/...");
              }
            }
          } 
        } else if (request.contains("fruitOrVeg?")) {

          Map<String, String> query_pairs = new LinkedHashMap<String, String>();
          int ind = 0;
          String head = null;
          String img = null;
          int test = 0;

          if(request.indexOf("?") > 1) {
            try{
              query_pairs = splitQuery(request.replace("fruitOrVeg?", ""));
              String type = query_pairs.get("type");
              String color = query_pairs.get("color");
              if (type.equalsIgnoreCase("vegetable")) {
                test++;
                if (color.equalsIgnoreCase("red")) {
                  ind = random.nextInt(red_veg.size());
                  head = (String) red_veg.keySet().toArray()[ind];
                  img = red_veg.get(head);
                }
                else if (color.equalsIgnoreCase("orange")) {
                  ind = random.nextInt(orange_veg.size());
                  head = (String) orange_veg.keySet().toArray()[ind];
                  img = orange_veg.get(head);
                }
                else if (color.equalsIgnoreCase("yellow")) {
                  ind = random.nextInt(yellow_veg.size());
                  head = (String) yellow_veg.keySet().toArray()[ind];
                  img = yellow_veg.get(head);
                }
                else if (color.equalsIgnoreCase("green")) {
                  ind = random.nextInt(green_veg.size());
                  head = (String) green_veg.keySet().toArray()[ind];
                  img = green_veg.get(head);
                }
                else if (color.equalsIgnoreCase("blue")) {
                  head = "Blue Corn";
                  img = "https://www.ediblenm.com/wp-content/uploads/2019/02/A84A5990-web.jpg";
                }
                else if (color.equalsIgnoreCase("purple")) {
                  ind = random.nextInt(purple_veg.size());
                  head = (String) purple_veg.keySet().toArray()[ind];
                  img = purple_veg.get(head);
                }
              } else if (type.equalsIgnoreCase("fruit")) {
                test++;
                if (color.equalsIgnoreCase("red")) {
                  ind = random.nextInt(red_fruit.size());
                  head = (String) red_fruit.keySet().toArray()[ind];
                  img = red_fruit.get(head);
                }
                else if (color.equalsIgnoreCase("orange")) {
                  ind = random.nextInt(orange_fruit.size());
                  head = (String) orange_fruit.keySet().toArray()[ind];
                  img = orange_fruit.get(head);
                }
                else if (color.equalsIgnoreCase("yellow")) {
                  ind = random.nextInt(yellow_fruit.size());
                  head = (String) yellow_fruit.keySet().toArray()[ind];
                  img = yellow_fruit.get(head);
                }
                else if (color.equalsIgnoreCase("green")) {
                  ind = random.nextInt(green_fruit.size());
                  head = (String) green_fruit.keySet().toArray()[ind];
                  img = green_fruit.get(head);
                }
                else if (color.equalsIgnoreCase("blue")) {
                  ind = random.nextInt(blue_fruit.size());
                  head = (String) blue_fruit.keySet().toArray()[ind];
                  img = blue_fruit.get(head);
                }
                else if (color.equalsIgnoreCase("purple")) {
                  ind = random.nextInt(purple_fruit.size());
                  head = (String) purple_fruit.keySet().toArray()[ind];
                  img = purple_fruit.get(head);
                }
              }

              File image = new File(img);
              builder.append("HTTP/1.1 200 OK\n");
              builder.append("Content-Type: text/html; charset=utf-8\n");
              builder.append("\n");
              builder.append("<html>");
              builder.append("<body>");
              builder.append("<h1>" + "The Fruit or Vegetable is: " + head + "<br>" + "</h1>");
              builder.append("<img src=").append('"').append(img).append('"').append(" style='max-width:500px;max-height:500px' />");
              builder.append("</body>");
              builder.append("</html>");

              
            }
            catch (StringIndexOutOfBoundsException e) {
              builder.append("HTTP/1.1 400 Bad Request\n");
              builder.append("Content-Type: text/html; charset=utf-8\n");
              builder.append("\n");
              builder.append("Error 400 Bad Request: Please enter a value for type and color. Example /fruitOrVeg?type=fruit&color=red");
            }
            catch (Exception e) {
              builder.append("HTTP/1.1 404 Not Found\n");
              builder.append("Content-Type: text/html; charset=utf-8\n");
              builder.append("\n");
              if (test == 0) {
                builder.append("Error 404 Not Found: Please input a type - fruit or vegetable");
              }
              else {
                builder.append("Error 404 Not Found: Please input a color - red, orange, yellow, green, blue, or purple");
              }
            }
          }
        } else if (request.contains("bored?")) {
            Map<String, String> query_pairs = new LinkedHashMap<String, String>();

            if (request.indexOf("?") > 1) {
            try {
              query_pairs = splitQuery(request.replace("bored?", ""));
              Integer participants = Integer.parseInt(query_pairs.get("participants"));
              String type = query_pairs.get("type");
              String json = fetchURL("https://www.boredapi.com/api/activity?participants=" + participants + "&type=" + type);
              //System.out.println(json);

              builder.append("HTTP/1.1 200 OK\n");
              builder.append("Content-Type: application/json; charset=utf-8\n");
              builder.append("\n");
              JSONObject obj = new JSONObject(json);
              builder.append("Suggestion: " + obj.get("activity"));
              builder.append("\n");
              builder.append("Price Approximation: " + obj.get("price"));
              builder.append("\n");
              //err = obj.get("error").toString();
            }
            catch (NumberFormatException e) {
              builder.append("HTTP/1.1 400 Bad Request\n");
              builder.append("Content-Type: text/html; charset=utf-8\n");
              builder.append("\n");
              builder.append("Error 400 Bad Request: Please enter an integer for the amount of participants");
            }
            catch (StringIndexOutOfBoundsException e) {
              builder.append("HTTP/1.1 400 Bad Request\n");
              builder.append("Content-Type: text/html; charset=utf-8\n");
              builder.append("\n");
              builder.append("Error 400 Bad Request: Please use the syntax - /bored?participants=1&type=music");
            }
            catch (Exception e) {
              if(!request.contains("type")) {
                builder.append("Error 400 Bad Request: Please include one of the following types in your query: relaxation, recreational, education, diy, music, cooking");
              }
              else {
                builder.append("Bored API has no suggestions for you, please try a different number of participants or a different activity type.");
              }
            }
          } 
        }else {
          // if the request is not recognized at all

          builder.append("HTTP/1.1 400 Bad Request\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append("I am not sure what you want me to do...");
        }

        // Output
        response = builder.toString().getBytes();
      }
    } catch (IOException e) {
      e.printStackTrace();
      response = ("<html>ERROR: " + e.getMessage() + "</html>").getBytes();
    }

    return response;
  }

  /**
   * Method to read in a query and split it up correctly
   * @param query parameters on path
   * @return Map of all parameters and their specific values
   * @throws UnsupportedEncodingException If the URLs aren't encoded with UTF-8
   */
  public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
    // "q=hello+world%2Fme&bob=5"
    String[] pairs = query.split("&");
    // ["q=hello+world%2Fme", "bob=5"]
    for (String pair : pairs) {
      int idx = pair.indexOf("=");
      query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
          URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
    }
    // {{"q", "hello world/me"}, {"bob","5"}}
    return query_pairs;
  }

  /**
   * Builds an HTML file list from the www directory
   * @return HTML string output of file list
   */
  public static String buildFileList() {
    ArrayList<String> filenames = new ArrayList<>();

    // Creating a File object for directory
    File directoryPath = new File("www/");
    filenames.addAll(Arrays.asList(directoryPath.list()));

    if (filenames.size() > 0) {
      StringBuilder builder = new StringBuilder();
      builder.append("<ul>\n");
      for (var filename : filenames) {
        builder.append("<li>" + filename + "</li>");
      }
      builder.append("</ul>\n");
      return builder.toString();
    } else {
      return "No files in directory";
    }
  }

  /**
   * Read bytes from a file and return them in the byte array. We read in blocks
   * of 512 bytes for efficiency.
   */
  public static byte[] readFileInBytes(File f) throws IOException {

    FileInputStream file = new FileInputStream(f);
    ByteArrayOutputStream data = new ByteArrayOutputStream(file.available());

    byte buffer[] = new byte[512];
    int numRead = file.read(buffer);
    while (numRead > 0) {
      data.write(buffer, 0, numRead);
      numRead = file.read(buffer);
    }
    file.close();

    byte[] result = data.toByteArray();
    data.close();

    return result;
  }

  /**
   *
   * a method to make a web request. Note that this method will block execution
   * for up to 20 seconds while the request is being satisfied. Better to use a
   * non-blocking request.
   * 
   * @param aUrl the String indicating the query url for the OMDb api search
   * @return the String result of the http request.
   *
   **/
  public String fetchURL(String aUrl) {
    StringBuilder sb = new StringBuilder();
    URLConnection conn = null;
    InputStreamReader in = null;
    try {
      URL url = new URL(aUrl);
      conn = url.openConnection();
      if (conn != null)
        conn.setReadTimeout(20 * 1000); // timeout in 20 seconds
      if (conn != null && conn.getInputStream() != null) {
        in = new InputStreamReader(conn.getInputStream(), Charset.defaultCharset());
        BufferedReader br = new BufferedReader(in);
        if (br != null) {
          int ch;
          // read the next character until end of reader
          while ((ch = br.read()) != -1) {
            sb.append((char) ch);
          }
          br.close();
        }
      }
      in.close();
    } catch (Exception ex) {
      System.out.println("Exception in url request:" + ex.getMessage());
    }
    return sb.toString();
  }
}
