package com.jesussoto.android.sparkle.sync;


import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Query;
import retrofit.http.Body;
import retrofit.mime.TypedOutput;

public interface WebServiceApi {

    public static final String PATH_DUMP = "/dump";
    public static final String PATH_QUERY = "/query";
    public static final String PATH_QUERY_DOWNLOAD = "/query_download";
    public static final String PATH_DOWNLOAD_RESULT = "/download_results";

    public static final String PARAM_FORMAT = "format";

    @POST(PATH_DUMP)
    public String dump();

    @POST(PATH_QUERY)
    public String query(@Body String sparqlQuery);

    @POST(PATH_QUERY)
    @Headers({"Content-Type: text/plain"})
    public String query(@Body TypedOutput sparqlQuery, @Query(PARAM_FORMAT) int format);

    @POST(PATH_QUERY_DOWNLOAD)
    public String queryDownload(@Body TypedOutput sparqlQuery, @Query(PARAM_FORMAT) int format);

    @POST(PATH_DOWNLOAD_RESULT)
    public void downloadResults(@Body String url);
}
