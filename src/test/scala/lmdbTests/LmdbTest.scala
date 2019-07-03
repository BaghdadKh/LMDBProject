package lmdbTests

import java.io.File

import org.lmdbjava.DbiFlags.MDB_CREATE
import org.lmdbjava.EnvFlags.MDB_NOSUBDIR
import org.scalatest.{FlatSpec, Matchers}
import utils.LMDB_v2


class foundValueTest extends FlatSpec with Matchers  {
  val myLmdb = new LMDB_v2
  val env  = myLmdb.createEnv()
  val env2 = myLmdb.openEnv(env , new File ("test2.txt"), MDB_NOSUBDIR)
  val db = myLmdb.openDb(env2,"my DB WriteTest2", MDB_CREATE)
  val txWrite = myLmdb.createWriteTx(env2)

  myLmdb.putOnLmdb(txWrite, db, myLmdb.createElement("K1"), myLmdb.createElement("V1"))
  myLmdb.commitToDb(txWrite)
  val txnRead = myLmdb.createReadTx(env2)
  val cursor = myLmdb.readFromDb(txnRead, db)

  //
  //  val cur = myLmdb.getCursor(cursor)
  //  val k:String = UTF_8.decode(cur.next().key()).toString


  "the cur " should "be K1" in {
    assert(txWrite.equals("K1"))
  }
}