package utils
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteBuffer.allocateDirect
import java.nio.charset.StandardCharsets.UTF_8

import main.ModularizeCode
import org.lmdbjava.Env.{Builder, create}
import org.lmdbjava._
import scalaz.zio.console.putStrLn
import scalaz.zio.{App, IO, UIO, ZIO}

class LMDB(){

  def createEnv(): UIO[Builder[ByteBuffer]] = {
    IO.succeed(create().setMapSize(10485760).setMaxDbs(1))
  }

  def closeTxn(txn:Txn[ByteBuffer]){
    IO.succeed(txn.close())
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

  def readFromDb(txn: Txn[ByteBuffer], db: Dbi[ByteBuffer]) = {
    val cursor: CursorIterator[ByteBuffer] = db.iterate(txn, KeyRange.all[ByteBuffer]())
    IO.succeed(cursor)
  }

  def hasNextCur(c: CursorIterator[ByteBuffer]) = for {
    has <- IO.succeed(c.hasNext)
  } yield (IO.succeed(has))

  def printValues(c: CursorIterator[ByteBuffer]): ZIO[ModularizeCode.Environment, Nothing, CursorIterator[ByteBuffer]] = for {
    flag <- hasNextCur(c)
    hasNext <- flag
    loop <- if (hasNext) {
      val kv = c.next();
      putStrLn(UTF_8.decode(kv.key()).toString + "  " + UTF_8.decode(kv.`val`()).toString).const(true)
    }
    else putStrLn("").const(false)
    _ <- if (loop) printValues(c) else IO.succeed("")
  } yield c
}