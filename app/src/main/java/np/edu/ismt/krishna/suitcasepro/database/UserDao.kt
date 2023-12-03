package np.edu.ismt.krishna.suitcasepro.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Query("Select * from user_table")
    fun getAllUsers(): List<User>

    //For Login
    @Query("Select * from user_table where email = :email and password = :password LIMIT 1")
    fun getSpecificUser(email: String, password: String): User?

    @Insert
    fun insertNewUser(user: User)


    @Query("SELECT * from user_table WHERE id = :userId")
    suspend fun getUsernameById(userId: Int): User?




    //Check if user already exist
    @Query("Select * from user_table where email = :userEmail")
    fun checkUserExist(userEmail: String): User?
}