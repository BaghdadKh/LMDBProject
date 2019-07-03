package utils

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteBuffer.allocateDirect
import java.nio.charset.StandardCharsets.UTF_8

import main.ModularizeCode
import org.lmdbjava.Env.{AlreadyClosedException, Builder, create}
import org.lmdbjava.EnvFlags.MDB_NOSUBDIR
import org.lmdbjava._
import scalaz.zio.console.putStrLn
import scalaz.zio.{App, IO, UIO, ZIO}

object LMDB_errorChecking extends App {
  def run(args: List[String]) =
    myApp.fold(_ => 1, _ => 0)

  val myApp =
    for {
      _ <- putStrLn("Helloooooooooooo")
      env <- createEnv()
      env2 <- IO.succeed(setSizeEnv(8852, env))
      envSize <- IO.succeed(env2).map(value => value match {
        case Left(l) => putStrLn("Error")
        case Right(r) => r.setMaxDbs(1)
      })
      _ <- envSize match {
        case e: Builder[ByteBuffer] => openEnv(IO.succeed(e), null, MDB_NOSUBDIR)
      }
    } yield ()

  def createEnv(): ZIO[Any, Throwable, Builder[ByteBuffer]] = for {
    environment <- IO.succeed(create())
  } yield (environment)

  def setSizeEnv(size: Int, env: Builder[ByteBuffer]): Either[String, Builder[ByteBuffer]] =
    if (size < 0) Left("The size is illegal , negative number ") else Right(env.setMapSize(size))

  def openEnv(env: ZIO[Any, Throwable, Builder[ByteBuffer]], lmdbFile: File, flag: EnvFlags) = for {
    environment <- env
    openedEnv <- if (lmdbFile != null) IO.succeed(environment.open(lmdbFile, flag)) else putStrLn("File not found ")
  } yield openedEnv

  def openDb(env: ZIO[Any, Throwable, Env[ByteBuffer]], lmdbName: String, flag: DbiFlags): ZIO[Any, Throwable, Dbi[ByteBuffer]] = for {
    environment <- env
    db <- IO.succeed(environment.openDbi(lmdbName, flag))
  } yield db

  def closeTxn(txn: Txn[ByteBuffer]): ZIO[Any, Throwable, Unit] = {
    IO.succeed(txn.close())
  }

  def createElement[A](value: A): ByteBuffer = {
    val bb = allocateDirect(200)
    bb.put(value.toString.getBytes(UTF_8)).flip
    bb
  }

  def createReadTx(env: UIO[Env[ByteBuffer]]): ZIO[Any, AlreadyClosedException, Txn[ByteBuffer]] = for {
    environment <- env
    txnRead <- IO.succeed(environment.txnRead())
  } yield txnRead

  def createWriteTx(env: UIO[Env[ByteBuffer]]): ZIO[Any, AlreadyClosedException, Txn[ByteBuffer]] = for {
    environment <- env
    txnWrite <- IO.succeed(environment.txnWrite())
  } yield txnWrite

  def putOnLmdb(tx: UIO[Txn[ByteBuffer]], db: UIO[Dbi[ByteBuffer]], key: ByteBuffer, value: ByteBuffer): ZIO[Any, Throwable, Boolean] = for {
    db2 <- db
    tx2 <- tx
    put <- IO.succeed(db2.put(tx2, key, value))
  } yield put

  def commitToDb(tx: UIO[Txn[ByteBuffer]]) = for {
    txn <- tx
    commit <- IO.succeed(txn.commit())
  } yield commit

  def readFromDb(txn: UIO[Txn[ByteBuffer]], dbi: UIO[Dbi[ByteBuffer]]): UIO[CursorIterator[ByteBuffer]] = for {
    tx <- txn
    db <- dbi
    cur <- IO.succeed(db.iterate(tx, KeyRange.all[ByteBuffer]()))
  } yield cur

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

  def getCursor(cursor: UIO[CursorIterator[ByteBuffer]]) = {
    unsafeRun(cursor)
  }
}