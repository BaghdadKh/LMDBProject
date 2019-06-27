package main

import org.lmdbjava.DbiFlags.MDB_CREATE
import org.lmdbjava.EnvFlags.MDB_NOSUBDIR
import scalaz.zio._
import scalaz.zio.console._
import utils.{Bank, LMDB}

object BankSceanario extends App {

  val lmdb = new LMDB()
  val bank = new Bank(lmdb)

  override def run(args: List[String]): ZIO[BankSceanario.Environment, Nothing, Int] = for {
    env <- lmdb.createEnv()
    env2 <- lmdb.openEnv(env, "bankTest.txt", MDB_NOSUBDIR)
    db <- lmdb.openDb(env2, "my DB utils.Bank", MDB_CREATE)
    txnWrite <- lmdb.createWriteTx(env2)
    _ <- bank.createAccount(txnWrite, db, "id1", 500)
    _ <- bank.createAccount(txnWrite, db, "id2", 400)
    _ <- bank.createAccount(txnWrite, db, "id3", 600)
    _ <- lmdb.commitToDb(txnWrite)
    txRead <- lmdb.createReadTx(env2)
    cursor <- lmdb.readFromDb(txRead, db)
    _ <- putStrLn("Initial Values ")
    _ <- lmdb.printValues(cursor)

    cursor2 <- lmdb.readFromDb(txRead, db)
    _ <- IO.succeed(lmdb.closeTxn(txnWrite))
    //transactions to draw from balance
    _ <- putStrLn("Values after Draw 40 from id2")
    txnWriteDraw <- lmdb.createWriteTx(env2)
    _  <-bank.withdraw(txnWriteDraw,db,cursor2,"id2",40.0)
    _ <-lmdb.commitToDb(txnWriteDraw)
    _ <- IO.succeed(lmdb.closeTxn(txRead))
    txRead2 <- lmdb.createReadTx(env2)
    cursor3 <- lmdb.readFromDb(txRead2, db)
    _ <- lmdb.printValues(cursor3)

    /************************/
    cursor4 <- lmdb.readFromDb(txRead2, db)
    _ <- IO.succeed(lmdb.closeTxn(txnWriteDraw))

    //transactions for add to balance
    _ <- putStrLn("Values after adding 50.0 to id1")
    txnWriteAdd <- lmdb.createWriteTx(env2)
    _  <-bank.addToBalance(txnWriteAdd,db,cursor4,"id1",50.0)
    _ <-lmdb.commitToDb(txnWriteAdd)
    _ <- IO.succeed(lmdb.closeTxn(txRead2))
    _ <- IO.succeed(lmdb.closeTxn(txnWriteAdd))
    txRead3 <- lmdb.createReadTx(env2)
    cursor5 <- lmdb.readFromDb(txRead3, db)
    _ <- lmdb.printValues(cursor5)

  } yield (0)




}