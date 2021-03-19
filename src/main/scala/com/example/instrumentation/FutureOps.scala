package com.example.instrumentation

import com.newrelic.api.agent.{Trace, Transaction}
import com.newrelic.api.agent.NewRelic

case class TransactionWrapper(val txn: Transaction) extends AnyVal;

object FutureOps {
  implicit def txnWrapper = {
    printTxnInfo()
    TransactionWrapper(NewRelic.getAgent().getTransaction())
  }
  def printTxnInfo() = {
    val txn = NewRelic.getAgent().getTransaction()
    println(s"[${Thread.currentThread.getName}]: txn, $txn, ${txn.isTransactionNameSet()}")
  }

  def traceBody[S](segmentName: String)(body: => S)(implicit txnWrapper: TransactionWrapper): S = {
    txnWrapper.txn.getToken().link()
    println(s"[${Thread.currentThread.getName}]: traceBody[$segmentName], Txn: ${txnWrapper.txn}")
    val segment = txnWrapper.txn.startSegment(segmentName)
    try {
      //   traceAsyncBody(body)
      body
    } finally {
      segment.end()
    }
  }

  def traceFunc[T, S](segmentName: String)(f: T => S)(implicit txnWrapper: TransactionWrapper): T => S = { t: T =>
    txnWrapper.txn.getToken().link()
    println(s"[${Thread.currentThread.getName}]: traceFunc[$segmentName], Txn: ${txnWrapper.txn}")
    val segment = txnWrapper.txn.startSegment(segmentName)
    try {
      //   traceAsyncFunc(f, t)
      f(t)
    } finally {
      segment.end()
    }
  }

  @Trace(async = true)
  private def traceAsyncBody[S](body: => S): S =
    body

  @Trace(async = true)
  private def traceAsyncFunc[T, S](f: T => S, t: T): S =
    f(t)

}
