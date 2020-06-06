package de.dis;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

public class TransactionBuffer {

  public TransactionBuffer(int pTransactionId) {
    m_Id = pTransactionId;
    m_Committed = false;
    m_Buffer = new Hashtable<>();
  }

  public int GetId() {
    return m_Id;
  }

  public void SetCommitted(boolean pIsCommitted) {
    m_Committed = pIsCommitted;
  }

  public boolean IsCommitted () {
    return m_Committed;
  }

  public void Write(Integer pPageId, String pData) {
    m_Buffer.put(pPageId, pData);
  }

  public Map<Integer, String> getReadOnlyBuffer() {
    return Collections.unmodifiableMap(m_Buffer);
  }

  private int m_Id;
  private boolean m_Committed;
  private Hashtable<Integer, String> m_Buffer;

}
