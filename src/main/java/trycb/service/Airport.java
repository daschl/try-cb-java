package trycb.service;

import com.couchbase.client.core.error.QueryServiceException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;
import trycb.model.Result;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class Airport {

    private static final Logger LOGGER = LoggerFactory.getLogger(Airport.class);


    /**
     * Find all airports.
     */
    public static Result<List<Map<String, Object>>> findAll(final Cluster cluster, final String bucket, final String params) {
        StringBuilder builder = new StringBuilder();
        builder.append("select airportname from `").append(bucket).append("` where ");

        if (params.length() == 3) {
            builder.append("faa = $val");
        } else if (params.length() == 4 && (params.equals(params.toUpperCase()) || params.equals(params.toLowerCase()))) {
            builder.append("icao = $val");
        } else {
            // The airport name should start with the parameter value.
            builder.append("POSITION(airportname, $val) = 0");
        }
        String query = builder.toString();

        logQuery(query);
        QueryResult result = null;
        try {
            result = cluster.query(query, QueryOptions.queryOptions().rawParams("$val", params));
        } catch (QueryServiceException e) {
            LOGGER.warn("Query failed with exception: " + e);
            throw new DataRetrievalFailureException("Query error", e);
        }

        List<JsonObject> resultObjects = result.allRowsAsObject();
        List<Map<String, Object>> data = new LinkedList<Map<String, Object>>();
        for (JsonObject obj: resultObjects) {
            data.add(obj.toMap());
        }
        return Result.of(data, query);
    }

    /**
     * Helper method to log the executing query.
     */
    private static void logQuery(String query) {
        LOGGER.info("Executing Query: {}", query);
    }

}
