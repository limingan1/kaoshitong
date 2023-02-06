package com.suntek.vdm.gw.welink.util;



import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

public final class RequestUtil {

    private static SimpleDateFormat format = new SimpleDateFormat(" EEEE, dd-MMM-yy kk:mm:ss zz");
    public static String filter(String message) {
        if(message == null) {
            return null;
        } else {
            char[] content = new char[message.length()];
            message.getChars(0, message.length(), content, 0);
            StringBuffer result = new StringBuffer(content.length + 50);

            for(int i = 0; i < content.length; ++i) {
                switch(content[i]) {
                    case 34:
                        result.append("\"");
               break;
            case 38:
               result.append("&");
               break;
            case 60:
               result.append("<");
               break;
            case 62:
               result.append(">");
               break;
            default:
               result.append(content[i]);
            }
         }

         return result.toString();
      }
   }

   public static String normalize(String path) {
      return normalize(path, true);
   }

   public static String normalize(String path, boolean replaceBackSlash) {
      if(path == null) {
         return null;
      } else {
         String normalized = path;
         if(replaceBackSlash && path.indexOf(92) >= 0) {
            normalized = path.replace('\\', '/');
         }

         if(!normalized.startsWith("/")) {
            normalized = "/" + normalized;
         }

         while(true) {
            int index = normalized.indexOf("//");
            if(index < 0) {
               while(true) {
                  index = normalized.indexOf("/./");
                  if(index < 0) {
                     while(true) {
                        index = normalized.indexOf("/../");
                        if(index < 0) {
                           return normalized.equals("/.")?"/":(normalized.equals("/..")?null:normalized);
                        }

                        if(index == 0) {
                           return null;
                        }

                        int index2 = normalized.lastIndexOf(47, index - 1);
                        normalized = normalized.substring(0, index2) + normalized.substring(index + 3);
                     }
                  }

                  normalized = normalized.substring(0, index) + normalized.substring(index + 2);
               }
            }

            normalized = normalized.substring(0, index) + normalized.substring(index + 1);
         }
      }
   }

   public static void parseParameters(Map map, String data, String encoding) throws UnsupportedEncodingException {
      if(data != null && data.length() > 0) {
         byte[] bytes = null;

         try {
            if(encoding == null) {
               bytes = data.getBytes();
            } else {
               bytes = data.getBytes(encoding);
            }
         } catch (UnsupportedEncodingException var5) {
            ;
         }

         parseParameters(map, (byte[])bytes, encoding);
      }

   }

   public static String URLDecode(String str) {
      return URLDecode((String)str, (String)null);
   }

   public static String URLDecode(String str, String enc) {
      return URLDecode((String)str, enc, false);
   }

   public static String URLDecode(String str, String enc, boolean isQuery) {
      if(str == null) {
         return null;
      } else {
         byte[] bytes = null;

         try {
            if(enc == null) {
               bytes = str.getBytes();
            } else {
               bytes = str.getBytes(enc);
            }
         } catch (UnsupportedEncodingException var5) {
            ;
         }

         return URLDecode((byte[])bytes, enc, isQuery);
      }
   }

   public static String URLDecode(byte[] bytes) {
      return URLDecode((byte[])bytes, (String)null);
   }

   public static String URLDecode(byte[] bytes, String enc) {
      return URLDecode((byte[])bytes, enc, false);
   }

   public static String URLDecode(byte[] bytes, String enc, boolean isQuery) {
      if(bytes == null) {
         return null;
      } else {
         int len = bytes.length;
         int ix = 0;

         int ox;
         byte e;
         for(ox = 0; ix < len; bytes[ox++] = e) {
            e = bytes[ix++];
            if(e == 43 && isQuery) {
               e = 32;
            } else if(e == 37) {
               e = (byte)((convertHexDigit(bytes[ix++]) << 4) + convertHexDigit(bytes[ix++]));
            }
         }

         if(enc != null) {
            try {
               return new String(bytes, 0, ox, enc);
            } catch (Exception var7) {
               var7.printStackTrace();
            }
         }

         return new String(bytes, 0, ox);
      }
   }

   private static byte convertHexDigit(byte b) {
      return b >= 48 && b <= 57?(byte)(b - 48):(b >= 97 && b <= 102?(byte)(b - 97 + 10):(b >= 65 && b <= 70?(byte)(b - 65 + 10):0));
   }

   private static void putMapEntry(Map map, String name, String value) {
      String[] newValues = null;
      String[] oldValues = (String[])((String[])map.get(name));
      if(oldValues == null) {
         newValues = new String[]{value};
      } else {
         newValues = new String[oldValues.length + 1];
         System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
         newValues[oldValues.length] = value;
      }

      map.put(name, newValues);
   }

   public static void parseParameters(Map map, byte[] data, String encoding) throws UnsupportedEncodingException {
      if(data != null && data.length > 0) {
         int ix = 0;
         int ox = 0;
         String key = null;
         String value = null;

         while(ix < data.length) {
            byte c = data[ix++];
            switch((char)c) {
            case 37:
               data[ox++] = (byte)((convertHexDigit(data[ix++]) << 4) + convertHexDigit(data[ix++]));
               break;
            case 38:
               value = new String(data, 0, ox, encoding);
               if(key != null) {
                  putMapEntry(map, key, value);
                  key = null;
               }

               ox = 0;
               break;
            case 43:
               data[ox++] = 32;
               break;
            case 61:
               if(key == null) {
                  key = new String(data, 0, ox, encoding);
                  ox = 0;
               } else {
                  data[ox++] = c;
               }
               break;
            default:
               data[ox++] = c;
            }
         }

         if(key != null) {
            value = new String(data, 0, ox, encoding);
            putMapEntry(map, key, value);
         }
      }

   }

   static {
      format.setTimeZone(TimeZone.getTimeZone("GMT"));
   }
}

