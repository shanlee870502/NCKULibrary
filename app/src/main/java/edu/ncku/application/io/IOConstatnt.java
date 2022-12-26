package edu.ncku.application.io;

/**
 *為了提高聚合性，使用這個介面來儲存常數值，可以避免一邊有改而一邊沒改的問題
 * 所以file跟network這兩個故package裡，會實作這個類別來取得常數
 */
public interface IOConstatnt {

    String UPCOMING_EVENT_FILE = "NCKU_Lib_Upcoming_Event";
    //String UPCOMING_EVENT_URL = "http://140.116.207.50/libweb/index.php?item=webActivity&lan=";
    String UPCOMING_EVENT_URL = "http://app.lib.ncku.edu.tw/libweb/index.php?item=webActivity&lan=";
    String UPCOMING_EVENT_URL_SSL = "https://app.lib.ncku.edu.tw/libweb/index.php?item=webActivity&lan=";


    String CONTACT_FILE = "NCKU_Lib_Contact_Info";
    String CONTACT_URL = "http://140.116.207.50/libweb/index.php?item=webOrganization&lan=";
    String CONTACT_URL_SSL = "https://app.lib.ncku.edu.tw/libweb/index.php?item=webOrganization&lan=";

    String FLOOR_INFO_FILE = "NCKU_Lib_Floor_Info";
    String FLOOR_INFO_URL = "http://140.116.207.50/libweb/index.php?item=webFloorplan&lan=";
    String FLOOR_INFO_URL_SSL = "https://app.lib.ncku.edu.tw/libweb/index.php?item=webFloorplan&lan=";


    String LIB_OPEN_TIME_FILE = "NCKU_Lib_Open_Time";
    String LIB_OPEN_TIME_URL = "http://140.116.207.50/libweb/libInfoJson.php";
    String LIB_OPEN_TIME_URL_SSL = "https://app.lib.ncku.edu.tw/libweb/libInfoJson.php";


    String NEWS_FILE = "News";
    String NEWS_URL = "http://140.116.207.50/libweb/index.php?item=webNews&lan=";
    String NEWS_URL_SSL = "https://app.lib.ncku.edu.tw/libweb/index.php?item=webNews&lan=";

    //以下還要修改SSL版本
    String ISBN = "ISBN";
    String ISBN_SEARCH_URL = "http://m.lib.ncku.edu.tw/catalogs/ISBNBibSearch.php?lan=%s&ISBN=%s";
    String ISBN_SEARCH_URL_SSL = "https://m.lib.ncku.edu.tw/catalogs/ISBNBibSearch.php?lan=%s&ISBN=%s";

    String KEYWORD = "keyword";
    String SEARCH_URL = "http://m.lib.ncku.edu.tw/catalogs/KeywordSearch%s.php";
    String SEARCH_URL_SSL = "https://m.lib.ncku.edu.tw/catalogs/KeywordSearch%s.php";
    String BIB_URL = "http://m.lib.ncku.edu.tw/catalogs/KeywordBibSearch.php?Keyword=%s&lan=%s";
    String BIB_URL_SSL = "https://m.lib.ncku.edu.tw/catalogs/KeywordBibSearch.php?Keyword=%s&lan=%s";

    //dorm page
    String Main_LIB_URL_SSL = "https://app.lib.ncku.edu.tw/push/functions/getVisitorNumber_main.php";
    String KnowLEDGE_URL_SSL = "https://app.lib.ncku.edu.tw/push/functions/getVisitorNumber_Knowledge.php";
    String Medlib_URL_SSL = "https://app.lib.ncku.edu.tw/push/functions/getVisitorNumber_medlib.php";
    String D24_URL_SSL = "https://app.lib.ncku.edu.tw/push/functions/getVisitorNumber_d24.php";
    String Xcollege_URL_SSL = "https://app.lib.ncku.edu.tw/push/functions/getVisitorNumber_xcollege.php";

    //dorm rule
    String LIB_RULE_URL_SSL = "https://app.lib.ncku.edu.tw/redirect.php?dest=LibRule&lan=";
    String KnowLEDGE_RULE_URL_SSL = "https://app.lib.ncku.edu.tw/redirect.php?dest=KnowledgeRule&lan=";
    String D24_RULE_URL_SSL = "https://app.lib.ncku.edu.tw/redirect.php?dest=D24Rule&lan=";
    String Xcollege_RULE_URL_SSL = "https://app.lib.ncku.edu.tw/redirect.php?dest=FutureVenueRule&lan=";


    //dorm info
    String Medlib_INFO_URL_SSL = "https://app.lib.ncku.edu.tw/redirect.php?dest=MedlibMoreInfo&lan=cht";
    String D24_INFO_URL_SSL = "https://www.facebook.com/NCKUSDAD/";
    String D24_INFO_URL_SSL_FB = "fb://page/156886274440";
    String Xcollege_INFO_URL_SSL = "https://www.facebook.com/NCKU.Future.Venue/";
    String Xcollege_INFO_URL_SSL_FB = "fb://page/106314247703479";

    //occupancy limit
    String OCC_LIMIT_URL_SSL = "https://app.lib.ncku.edu.tw/libweb/seatnum.php";

    boolean showLogMsg = true;
}
