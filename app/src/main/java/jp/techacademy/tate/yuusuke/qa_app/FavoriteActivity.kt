package jp.techacademy.tate.yuusuke.qa_app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.math.log

class FavoriteActivity : AppCompatActivity() {

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mQuestionArrayList: ArrayList<Favorite>
    private lateinit var mAdapter: FavoritesListAdapter

    private lateinit var mFirebase: FirebaseDatabase

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val title = map["title"] ?: ""
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""
            val genre = map["genre"] ?: ""
            val questionUid = map["questionUid"] ?: ""


            val favorite = Favorite(title, body, name, uid, genre, questionUid)
            mQuestionArrayList.add(favorite)
            mAdapter.notifyDataSetChanged()

        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)
        title = "お気に入り"

        val user = FirebaseAuth.getInstance().currentUser!!.uid

        mFirebase = FirebaseDatabase.getInstance()

        mDatabaseReference = FirebaseDatabase.getInstance().reference

        mListView = findViewById(R.id.favoriteListView)
        mAdapter = FavoritesListAdapter(this)
        mQuestionArrayList = ArrayList<Favorite>()
        mAdapter.notifyDataSetChanged()

        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        mListView.adapter = mAdapter

        val favData = mDatabaseReference.child(FavoritePATH).child(user)
        favData.addChildEventListener(mEventListener)


        mListView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("favorite", mQuestionArrayList[position])
            intent.putExtra("num", 2)
            startActivity(intent)
        }
    }
}
