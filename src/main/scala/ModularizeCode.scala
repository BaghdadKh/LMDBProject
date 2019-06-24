import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteBuffer.allocateDirect
import java.nio.charset.StandardCharsets.UTF_8

import org.lmdbjava.DbiFlags.MDB_CREATE
import org.lmdbjava.Env.{Builder, create}
import org.lmdbjava.EnvFlags.MDB_NOSUBDIR
import org.lmdbjava.{Env, Txn, _}
import scalaz.zio.console._
import scalaz.zio.{IO, UIO, _}

object ModularizeCode extends App {

  //Trying on Enviroments
  //val s: ZIO[Env[ByteBuffer], Unit, String] = IO.succeedLazy("hello World ")
  //  val result:ZIO[Env[ByteBuffer], Unit, String] = result.provide(env)
  //define LMDB environment

  val myLmdb = new LMDB()

  override def run(args: List[String]) =
    for {
      env <- myLmdb.createEnv()
      env2 <- myLmdb.openEnv(env, "writeTest.txt", MDB_NOSUBDIR)
      db <- myLmdb.openDb(env2, "my DB WriteTest", MDB_CREATE)
      tx <- myLmdb.createWriteTx(env2)
      _ <- myLmdb.putOnLmdb(tx, db, myLmdb.createElement("k7"), myLmdb.createElement("V7"))
      _ <- myLmdb.commitToDb(tx)
      _ <- putStrLn("Hello")
    } yield (0)

}

class LMDB() {

  def createEnv(): UIO[Builder[ByteBuffer]] = {
    IO.succeed(create().setMapSize(10485760).setMaxDbs(1))
  }

  def openEnv(env: Builder[ByteBuffer], lmdbFile: String, flag: EnvFlags) = {
    val file = new File(lmdbFile)
    IO.succeed(env.open(file, flag))
  }

  def openDb(env: Env[ByteBuffer], lmdbName: String, flag: DbiFlags) = {
    IO.succeed(env.openDbi(lmdbName, flag))
  }

  def createElement[A](value: A): ByteBuffer = {
    val bb = allocateDirect(200)
    bb.put(value.toString.getBytes(UTF_8)).flip
    bb
    //IO.succeed(bb)
  }

  def createReadTx(env: Env[ByteBuffer]) = {
    IO.succeed(env.txnRead())
  }

  def createWriteTx(env: Env[ByteBuffer]) = {
    IO.succeed(env.txnWrite())
  }

  def putOnLmdb(tx: Txn[ByteBuffer], db: Dbi[ByteBuffer], key: ByteBuffer, value: ByteBuffer) = {
    IO.succeed(db.put(tx, key, value))
  }

  def commitToDb(tx: Txn[ByteBuffer]) = {
    IO.succeed(tx.commit())
  }

  //trying Read from LMDB
  /** Error:(75, 57) value withFilter is not a member of Iterable[org.lmdbjava.CursorIterator.KeyVal[java.nio.ByteBuffer]]
    * for (kv:CursorIterator.KeyVal[ByteBuffer] <- cursor.iterable) { **/
  //  def readFromDb(txn: Txn[ByteBuffer], db: Dbi[ByteBuffer]) = {
  //    val cursor: CursorIterator[ByteBuffer] = db.iterate(txn, KeyRange.all[ByteBuffer]())
  //    for (kv:CursorIterator.KeyVal[ByteBuffer] <- cursor.iterable) {
  //      val key = kv.key()
  //      val value = kv.`val`
  //      println(UTF_8.decode(key) + " " + UTF_8.decode(value).toString)
  //    }
  //  }
}