package ru.mishenko.maksim.fcmtopics

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import ru.mishenko.maksim.fcmtopics.ui.theme.FCMTopicsTheme
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FCMTopicsTheme {
                val eventList by EventList.mutableEventList.collectAsState()
                val topic = "myTopic"
                LaunchedEffect(Unit) {
                    FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnSuccessListener {
                        Toast.makeText(baseContext, "Subscribed $topic", Toast.LENGTH_LONG).show()
                    }.addOnFailureListener {
                        Toast.makeText(baseContext, "Failed to Subscribe $topic", Toast.LENGTH_LONG)
                            .show()
                    }
                }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column() {
                        Column() {
                            eventList.forEach {
                                Text(it)
                            }
                        }
                        Button(onClick = {
                            /*val message = RemoteMessage.Builder("topic/$topic")
                                .addData("value", "hello world")
                                .build()
                            FirebaseMessaging.getInstance().send(message)*/
                            sendMessage("value", "hello world", topic)
                        }) {

                        }
                    }
                }
            }
        }
    }
}

fun sendMessage(title: String, content: String, topic: String) {
    GlobalScope.launch {
        val endpoint = "https://fcm.googleapis.com/fcm/send"
        try {
            val url = URL(endpoint)
            val httpsURLConnection: HttpsURLConnection =
                withContext(Dispatchers.IO) {
                    url.openConnection()
                } as HttpsURLConnection
            httpsURLConnection.readTimeout = 10000
            httpsURLConnection.connectTimeout = 15000
            httpsURLConnection.requestMethod = "POST"
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true

            val key =
                "AAAAXRYmXFQ:APA91bHlcLPVmzrhoWBONH85odu2qC0p5hRWuL4X6YDt4g-ZuuMSdNJDzl1GRwTd06zAgc_kXvrGPBM3_lgAvjiDT0TxH0TVpYZFdJY_V_5cd73KaH_rdEYk5b-VifE_tkHcMRr9lH4w "

            // Adding the necessary headers
            httpsURLConnection.setRequestProperty("authorization", "key=$key")
            httpsURLConnection.setRequestProperty("Content-Type", "application/json")

            // Creating the JSON with post params
            val body = JSONObject()

            val data = JSONObject()
            data.put(title, content)
            body.put("data", data)

            body.put("to", "/topics/$topic")

            val outputStream: OutputStream = BufferedOutputStream(httpsURLConnection.outputStream)
            val writer = BufferedWriter(OutputStreamWriter(outputStream, "utf-8"))
            writer.write(body.toString())
            writer.flush()
            writer.close()
            outputStream.close()
            val responseCode: Int = httpsURLConnection.responseCode
            val responseMessage: String = httpsURLConnection.responseMessage
            Log.d("Response:", "$responseCode $responseMessage")
            var result = String()
            var inputStream: InputStream? = null
            inputStream = if (responseCode in 400..499) {
                httpsURLConnection.errorStream
            } else {
                httpsURLConnection.inputStream
            }

            if (responseCode == 200) {
                Log.e("Success:", "notification sent $title \n $content")
                // The details of the user can be obtained from the result variable in JSON format
            } else {
                Log.e("Error", "Error Response")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FCMTopicsTheme {
        Greeting("Android")
    }
}