package vipul.com.kotlincoroutinesadapterexample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MainActivity : AppCompatActivity() {

    private lateinit var uiDispatch: CoroutineDispatcher
    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val retrofit = Retrofit.Builder()
                .baseUrl("https://example.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()

        val endpoint = retrofit.create(ApiEndpoint::class.java)

        uiDispatch = Dispatchers.Main
        job = Job()

        GlobalScope.launch(Dispatchers.Main + job) {
            progressBar.visibility = View.VISIBLE

            delay(3000) // Mocking delay ;)
            val peopleResponse = endpoint.getPeople().await()

            if (peopleResponse.isSuccessful) {
                peopleResponse.body()?.let {
                    Toast.makeText(this@MainActivity, it.toString(), Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this@MainActivity, "Some error occurred!", Toast.LENGTH_LONG).show()
            }

            progressBar.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!job.isCancelled) {
            job.cancel()
        }
    }

    interface ApiEndpoint {
        @GET("http://www.json-generator.com/api/json/get")
        fun getPeople(): Deferred<Response<People>>
    }

    data class Person(val name: String, val age: Int, val location: String)
    data class People(val people: Array<Person>)
}
