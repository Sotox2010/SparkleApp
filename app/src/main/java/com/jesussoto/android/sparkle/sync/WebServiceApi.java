package com.jesussoto.android.sparkle.sync;


import retrofit.http.POST;
import retrofit.http.Query;
import retrofit.http.Body;

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
    public String query(@Body String sparqlQuery, @Query(PARAM_FORMAT) String format);

    @POST(PATH_QUERY_DOWNLOAD)
    public String queryDownload(@Body String sparqlQuery, @Query(PARAM_FORMAT) String format);

    @POST(PATH_DOWNLOAD_RESULT)
    public void downloadResults(@Body String url);
}
