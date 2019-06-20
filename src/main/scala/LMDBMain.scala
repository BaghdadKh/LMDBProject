import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteBuffer.allocateDirect
import java.nio.charset.StandardCharsets.UTF_8

import org.lmdbjava.DbiFlags.MDB_CREATE
import org.lmdbjava.Env.create
import org.lmdbjava.EnvFlags.MDB_NOSUBDIR

import org.lmdbjava.KeyRange
// simple code to read from LMDB
object LMDBMain {
  def main(args: Array[String]): Unit = {
  }
  //define LMDB environment
  val DB_NAME = "my DB"
  val file = new File("test.txt")
  val env2 = create().setMapSize(10485760).setMaxDbs(1)
  val env = env2.open(file, MDB_NOSUBDIR)
  val db = env.openDbi(DB_NAME, MDB_CREATE)

  //declare key,value record 1
  val key = allocateDirect(env.getMaxKeySize)
  val value = allocateDirect(700)
  key.put("Greeting".getBytes(UTF_8)).flip
  value.put("Hello World".getBytes(UTF_8)).flip
  db.put(key, value)

  //declare key,value record 1
  val key2 = allocateDirect(env.getMaxKeySize)
  val value2 = allocateDirect(700)
  key2.put("Greeting2".getBytes(UTF_8)).flip
  value2.put("Hello World2".getBytes(UTF_8)).flip
  db.put(key2, value2)

  //fetching data from db
  val txn = env.txnRead
  try {
    val fetchedVal: ByteBuffer = txn.`val`()
    println(UTF_8.decode(fetchedVal).toString())
    txn.commit()
  } finally if (txn != null) txn.close()

}