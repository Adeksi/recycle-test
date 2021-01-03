package com.example.recycle

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    
    private var items = generateFakeValues()
    private val keyItemState = "items_state"
    private val keyItemBuffer = "items_buffer"
    private val keyItemsStartVal = "items_start_val"
    private var recyclerView: RecyclerView? = null


    private var itemsBuffer = mutableListOf<String>()
    private var startVal:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        val orientation = resources.configuration.orientation
        recyclerView!!.layoutManager =
            if (orientation != Configuration.ORIENTATION_LANDSCAPE) GridLayoutManager(this, 2)
            else {
                GridLayoutManager(this, 4)
            }
        recyclerView!!.adapter = MyAdapter(items, itemsBuffer)
        addItem()
    }

    private fun addItem(){
        startVal = items.size
//        Log.d("Cor", "Here")
        GlobalScope.launch {
//            Log.d("Cor", "Started")
            while (true) {
                delay(5000L)
                Log.d("Size", items.size.toString())
                val itemIndex = Random.nextInt(0,items.size+1)

                when (items.size) {
                    0 -> {
                        if (itemsBuffer.size == 0){
                            items.add(startVal++.toString())
                        } else {
                            val item = itemsBuffer[itemsBuffer.lastIndex]
                            items.add(item)
                            itemsBuffer.remove(item)
                        }
                        runOnUiThread {
                            (recyclerView!!.adapter as MyAdapter).notifyItemInserted(0)
                        }
                    }
                    else -> {
                        if (itemsBuffer.size == 0){
                            items.add(itemIndex, startVal++.toString())
                        } else {
                            val item = itemsBuffer[itemsBuffer.lastIndex]
                            items.add(itemIndex, item)
                            itemsBuffer.remove(item)
                        }
                        runOnUiThread {
                            (recyclerView!!.adapter as MyAdapter).notifyItemInserted(itemIndex)
                        }
                    }
                }
//                Log.d("ItemIndex", itemIndex.toString())
                Log.d("ItemsBuffer", itemsBuffer.toString())
            }
        }
    }


    override fun onSaveInstanceState(state: Bundle) {
        state.run{
            state.putStringArray(keyItemState, items.toTypedArray())
            state.putStringArray(keyItemBuffer, itemsBuffer.toTypedArray())
            state.putInt(keyItemsStartVal, startVal)
        }
        super.onSaveInstanceState(state)
    }


    override fun onRestoreInstanceState(state: Bundle) {
        super.onRestoreInstanceState(state)
        state.run{
            itemsBuffer = state.getStringArray(keyItemBuffer)!!.toMutableList()
//            Log.d("Items", items.toString())
            items = state.getStringArray(keyItemState)!!.toMutableList()
            recyclerView!!.adapter = MyAdapter(items, itemsBuffer)
//            Log.d("Items after restore", items.toString())
            startVal = state.getInt(keyItemsStartVal)
        }
    }


    private fun generateFakeValues(): MutableList<String> {
        val values = mutableListOf<String>()
        for (i in 0..15){
            values.add("$i")
        }
        return values
    }


    class MyAdapter(string: MutableList<String>, private var itemsBuffer: MutableList<String>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        private val items: MutableList<String> = string

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_item_view, parent, false)
                        .run {
                            ViewHolder(this)
                        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder) {
                bind(items[position])
            }

        }

        inner class ViewHolder(
                itemView: View,
                private val textView: TextView = itemView.findViewById(R.id.text_list_item)
        ) : RecyclerView.ViewHolder(itemView) {
            private val delBtn: Button = itemView.findViewById(R.id.delBtn)
            init {
                delBtn.setOnClickListener(remove())
            }

            private fun remove(): (View) -> Unit = {
                layoutPosition.also { currentPosition ->
                    try{
//                        Log.d("CurPos", currentPosition.toString())
//                        Log.d("Item on pos", items[currentPosition])
                        itemsBuffer.add(items[currentPosition])
                        items.removeAt(currentPosition)
                        notifyItemRemoved(currentPosition)
                    }
                    catch (e:ArrayIndexOutOfBoundsException) {
                        Log.d("Except", "Exx")
                    }
                }
            }
            fun bind(text: String) {
                textView.text = text
            }
        }
    }
}