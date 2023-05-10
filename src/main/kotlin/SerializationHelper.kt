import com.google.gson.Gson
import org.json.JSONObject

/**
 * Provides helper methods to serialize json text sent by the socket io interface
 * and convert objects to json objects.
 */
class SerializationHelper {
    val gson = Gson()

    /**
     * Converts a given object to a [JSONObject]
     * @param data object to be converted
     * */
    fun <T> serialize(data: T): JSONObject {
        // serialize to json string using gson
        val json = Gson().toJson(data)
        // create JSONObject vom json string
        return JSONObject(json)
    }

    /**
     * Converts the first json object of the socketIO-data to the parameterised class type object
     * @param socketIOData data array that is sent along with the socket io event
     * */
    inline fun <reified T> deserialize(socketIOData: Array<Any>): T {
        // get string for first json object of the array
        val json = (socketIOData.first() as JSONObject).toString()
        // deserialize json string using gson
        return gson.fromJson(json, T::class.java)
    }
}