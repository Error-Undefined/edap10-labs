import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import client.view.ProgressItem;
import client.view.StatusWindow;
import client.view.WorklistItem;
import network.Sniffer;
import network.SnifferCallback;
import rsa.Factorizer;
import rsa.ProgressTracker;

public class CodeBreaker implements SnifferCallback {

  private final JPanel workList;
  private final JPanel progressList;

  private final JProgressBar mainProgressBar;

  private final ExecutorService pool;

  private final Lock reportMutex; // Needed for mutual exclusion in progress bar update

  // -----------------------------------------------------------------------

  private CodeBreaker() {
    StatusWindow w = new StatusWindow();

    workList = w.getWorkList();
    progressList = w.getProgressList();
    mainProgressBar = w.getProgressBar();

    pool = Executors.newFixedThreadPool(2);

    reportMutex = new ReentrantLock();

    w.enableErrorChecks();
  }

  // -----------------------------------------------------------------------

  public static void main(String[] args) {

    /*
     * Most Swing operations (such as creating view elements) must be performed in
     * the Swing EDT (Event Dispatch Thread).
     * 
     * That's what SwingUtilities.invokeLater is for.
     */

    SwingUtilities.invokeLater(() -> {
      CodeBreaker codeBreaker = new CodeBreaker();
      new Sniffer(codeBreaker).start();
    });
  }

  // -----------------------------------------------------------------------

  /** Called by a Sniffer thread when an encrypted message is obtained. */
  @Override
  public void onMessageIntercepted(String message, BigInteger n) {

    SwingUtilities.invokeLater(() -> {
      JButton itemButton = new JButton("Break");
      WorklistItem workListItem = new WorklistItem(n, message);
      // System.out.println("message intercepted (N=" + n + ")...");
      workListItem.add(itemButton);
      workList.add(workListItem);
      itemButton.addActionListener((e) -> onBreakButtonClick(workListItem, message, n));
    });
  }

  private void onBreakButtonClick(WorklistItem workListItem, String message, BigInteger n) {
    SwingUtilities.invokeLater(() -> {
      workList.remove(workListItem);
      ProgressItem progressItem = new ProgressItem(n, message);
      progressList.add(progressItem);
      ProgressReport progressReport = new ProgressReport(progressItem);

      JButton cancelButton = new JButton("Cancel");

      JButton removeButton = new JButton("Remove");
      removeButton.addActionListener((e) -> onRemoveButtonClick(progressItem, progressReport));

      Runnable crackRunnable = () -> {
        try {
          String decrypted = Factorizer.crack(message, n, (i) -> progressReport.onProgress(i));
          progressReport.onComplete(decrypted);
          // On completion, add a "remove" button
          progressItem.remove(cancelButton);
          progressItem.add(removeButton);
        } catch (InterruptedException e) {
          throw new Error(e);
        }
      };
      progressItem.add(cancelButton);

      Future<?> crackFuture = pool.submit(crackRunnable);
      cancelButton.addActionListener(
          (e) -> onCancelButtonClick(cancelButton, removeButton, progressItem, progressReport, crackFuture));
    });
  }

  private void onRemoveButtonClick(ProgressItem item, ProgressReport progressReport) {
    progressList.remove(item);
    progressReport.onRemove();
  }

  private void onCancelButtonClick(JButton cancelButton, JButton removeButton, ProgressItem item,
      ProgressReport progressReport, Future<?> crackFuture) {
    if (crackFuture.cancel(true)) {
      progressReport.onCancel();
      item.remove(cancelButton);
      item.add(removeButton);
    }
  }

  private class ProgressReport {
    private ProgressItem item;
    private int totalProgress;

    public ProgressReport(ProgressItem item) {
      SwingUtilities.invokeLater(() -> {
        this.item = item;
        totalProgress = 0;
        reportMutex.lock();

        mainProgressBar.setMaximum(mainProgressBar.getMaximum() + 1000000);

        reportMutex.unlock();
      });
    }

    public void onProgress(int ppmDelta) {
      SwingUtilities.invokeLater(() -> {
        totalProgress += ppmDelta;
        reportMutex.lock();
        item.getProgressBar().setValue(totalProgress);
        mainProgressBar.setValue(mainProgressBar.getValue() + ppmDelta);
        reportMutex.unlock();
        // System.out.println("Progress: " + totalProgress + "/1000000");
      });
    }

    public void onComplete(String decrypted) {
      SwingUtilities.invokeLater(() -> {
        item.getTextArea().setText(decrypted);
      });
    }

    public void onRemove() {
      SwingUtilities.invokeLater(() -> {
        reportMutex.lock();
        mainProgressBar.setValue(mainProgressBar.getValue() - 1000000);
        mainProgressBar.setMaximum(mainProgressBar.getMaximum() - 1000000);
        reportMutex.unlock();
      });
    }

    public void onCancel() {
      SwingUtilities.invokeLater(() -> {
        item.getTextArea().setText("Cancelled");
        item.getProgressBar().setValue(1000000);
        int toAdd = 1000000 - totalProgress;
        reportMutex.lock();
        mainProgressBar.setValue(mainProgressBar.getValue() + toAdd);
        reportMutex.unlock();
      });
    }
  }
}
