/*
 * Autopsy Forensic Browser
 *
 * Copyright 2011-2015 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.casemodule;

import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.coreutils.MessageNotifyUtil;

/**
 * This class enables capturing errors and batching them for reporting on a
 * no-more-than-x number of seconds basis. When created, you specify the minimum
 * time between user notifications. When the time between notifications has
 * expired, the next error encountered will cause a report to be shown to the
 * user.
 */
class IntervalErrorReportData {

    private final Case currentCase;
    private long newProblems;
    private long totalProblems;
    private long lastReportedDate;
    private final int milliSecondsBetweenReports;
    private final String message;

    /**
     * Create a new IntervalErrorReprotData instance and subscribe for TSK error
     * notifications for the current case.
     *
     * @param currentCase           Case for which TSK errors should be tracked
     *                              and displayed.
     * @param secondsBetweenReports Minimum number of seconds between reports.
     *                              It will not warn more frequently than this.
     * @param message               The message that will be shown when warning
     *                              the user
     */
    IntervalErrorReportData(Case currentCase, int secondsBetweenReports, String message) {
        this.newProblems = 0;
        this.totalProblems = 0;
        this.lastReportedDate = 0; // arm the first warning by choosing zero
        this.milliSecondsBetweenReports = secondsBetweenReports * 1000;  // convert to milliseconds
        this.message = message;
        this.currentCase = currentCase;
        this.currentCase.getSleuthkitCase().addErrorObserver(this.currentCase);
    }
    
    /**
     * Un-subscribe from TSK error notifications for current case.
     */
    void shutdown() {
        this.currentCase.getSleuthkitCase().removeErrorObserver(this.currentCase);
    }

    /**
     * Call this to add problems to the class. When the time threshold is met
     * (or if this is the first problem encountered), a warning will be shown to
     * the user.
     *
     * @param context      The context in which the error occurred.
     * @param errorMessage A description of the error that occurred.
     */
    void addProblems(String context, String errorMessage) {
        this.newProblems += 1;
        this.totalProblems += newProblems;

        long currentTimeStamp = System.currentTimeMillis();
        if ((currentTimeStamp - lastReportedDate) > milliSecondsBetweenReports) {
            this.lastReportedDate = currentTimeStamp;
            MessageNotifyUtil.Notify.error(message, context + ", " + errorMessage + " "
                    + this.newProblems + " "
                    + NbBundle.getMessage(IntervalErrorReportData.class, "IntervalErrorReport.NewIssues")
                    + " " + this.totalProblems + " "
                    + NbBundle.getMessage(IntervalErrorReportData.class, "IntervalErrorReport.TotalIssues")
                    + ".");
            this.newProblems = 0;
        }
    }
}
