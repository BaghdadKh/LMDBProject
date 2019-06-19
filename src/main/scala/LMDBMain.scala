import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteBuffer.allocateDirect
import java.nio.charset.StandardCharsets.UTF_8

import org.lmdbjava.DbiFlags.MDB_CREATE
import org.lmdbjava.Env.create
import org.lmdbjava.EnvFlags.MDB_NOSUBDIR

object LMDBMain {
  val DB_NAME = "my DB"
  def main(args: Array[String]): Unit = {
    val obj = new LMDBTest2
    obj.test()
  }

}
class LMDBTest2 {
  def test () {
    val file = new File("C:\\Users\\Lenovo\\Desktop\\Temp\\test.txt")
    val env2 = create().setMapSize(10485760).setMaxDbs(1)
    val env = env2.open(file,MDB_NOSUBDIR)
    val db = env.openDbi(LMDBMain.DB_NAME, MDB_CREATE)

    val key = allocateDirect(env.getMaxKeySize)
    val value = allocateDirect(700)

    val key2 = allocateDirect(env.getMaxKeySize)
    val value2= allocateDirect(700)
    key.put("Greeting".getBytes(UTF_8)).flip
    value.put("Hello World".getBytes(UTF_8)).flip
    db.put(key, value)

    key2.put("Greeting2".getBytes(UTF_8)).flip
    value2.put("Hello World2".getBytes(UTF_8)).flip
    db.put(key2, value2)
    val txn = env.txnRead
    try {
      val found : ByteBuffer  = db.get(txn,key)
      val fetchedVal : ByteBuffer  = txn.`val`()
      println(UTF_8.decode(fetchedVal).toString())
      txn.commit()
    } finally if (txn != null) txn.close()

    val txn2 = env.txnRead
    try {
      val found2 : ByteBuffer  = db.get(txn2,key2)
      val fetchedVal2 : ByteBuffer  = txn2.`val`()
      println(UTF_8.decode(fetchedVal2).toString())
      txn2.commit()
    } finally if (txn2 != null) txn2.close()

    env.close()
  }
}