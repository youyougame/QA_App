package jp.techacademy.tate.yuusuke.qa_app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.math.log

class FavoriteActivity : AppCompatActivity() {

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mFavoriteArrayList: ArrayList<Favorite>
    private lateinit var mAdapter: FavoritesListAdapter
    private var genreInt = 0

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
            var imageString = ""

            var aBody = ""
            var aName = ""
            var aUid = ""
            var aAnswerUid = map["answers"].toString()



            val user = FirebaseAuth.getInstance().currentUser!!.uid
            mDatabaseReference = FirebaseDatabase.getInstance().reference

            val queImage = mDatabaseReference.child(ContentsPATH).child(genre).child(questionUid)
            queImage.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    val valuetest = dataSnapshot.value
                    imageString = valuetest.toString()
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })

            val ansData = mDatabaseReference.child(ContentsPATH).child(genre).child(questionUid)
                .child(AnswersPATH)
            ansData.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val valuetest = dataSnapshot.value
                    if (valuetest != null) {
                        val value = valuetest as Map<String, String>
                        aBody = value["body"] ?: ""
                        aName = value["name"] ?: ""
                        aUid = value["uid"] ?: ""
                    }


                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })

            val bytes =
                if (imageString.isNotEmpty()) {
                    Base64.decode(imageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }

            val answerArrayList = ArrayList<Answer>()
            val answer = Answer(aBody, aName, aUid, aAnswerUid)
            answerArrayList.add(answer)

            val favorite = Favorite(title, body, name, uid)

            val question = Question(
                title, body, name, uid, dataSnapshot.key ?: "",
                genre.toInt(), bytes, answerArrayList
            )

            mFavoriteArrayList.add(favorite)

            mQuestionArrayList.add(question)
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

//        val user = FirebaseAuth.getInstance().currentUser!!.uid

        mFirebase = FirebaseDatabase.getInstance()

        mDatabaseReference = FirebaseDatabase.getInstance().reference

        mListView = findViewById(R.id.favoriteListView)
        mAdapter = FavoritesListAdapter(this)
        mFavoriteArrayList = ArrayList<Favorite>()
        mAdapter.notifyDataSetChanged()

//        val favData = mDatabaseReference.child(FavoritePATH).child(user)
//        favData.addChildEventListener(mEventListener)



        mQuestionArrayList = ArrayList<Question>()


        mListView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val user = FirebaseAuth.getInstance().currentUser!!.uid

        val favData = mDatabaseReference.child(FavoritePATH).child(user)
        favData.addChildEventListener(mEventListener)

        mFavoriteArrayList.clear()
        mAdapter.setQuestionArrayList(mFavoriteArrayList)
        mListView.adapter = mAdapter

        mQuestionArrayList = ArrayList<Question>()


        mListView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }
    }
}