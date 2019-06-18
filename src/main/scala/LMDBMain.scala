import java.io.File
import java.nio.ByteBuffer.allocateDirect
import java.nio.charset.StandardCharsets.UTF_8
import org.lmdbjava.DbiFlags.MDB_CREATE
import org.lmdbjava.Env.create


object LMDBMain {
  val DB_NAME = "my DB"
  def main(args: Array[String]): Unit = {
    val obj = new LMDBTest2
    obj.test()
  }
}
class LMDBTest2 {
  def test () {
    val file = new File("C:\\Users\\Lenovo\\Desktop\\NEws\\Mar\\LMDB\\LMDBProject\\tempFolder\\test.txt")
    val env = create.setMapSize(1024*1024).setMaxDbs(1).open(file)
    val db = env.openDbi(LMDBMain.DB_NAME, MDB_CREATE)
    val key = allocateDirect(env.getMaxKeySize)
    val `val` = allocateDirect(700)
    key.put("greeting".getBytes(UTF_8)).flip
    `val`.put("Hello world".getBytes(UTF_8)).flip
    val valSize = `val`.remaining
    db.put(key, `val`)
    try {
      val txn = env.txnRead
      try {
        val found = db.get(txn, key)
        val fetchedVal = txn.`val`
      } finally if (txn != null) txn.close()
    }
    env.close()
  }
}
