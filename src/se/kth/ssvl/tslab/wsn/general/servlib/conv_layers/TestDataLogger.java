/*
 *	  This file is part of the Bytewalla Project
 *    More information can be found at "http://www.tslab.ssvl.kth.se/csd/projects/092106/".
 *    
 *    Copyright 2009 Telecommunication Systems Laboratory (TSLab), Royal Institute of Technology, Sweden.
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *    
 */
package se.kth.ssvl.tslab.wsn.general.servlib.conv_layers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import se.kth.ssvl.tslab.wsn.general.servlib.bundling.Bundle;
import se.kth.ssvl.tslab.wsn.general.bpf.BPF;

/**
 * Utility class to log the upload/download data generated by the CLConnection.
 * 
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 */
public class TestDataLogger {

	/**
	 * String TAG for supporting Android Logging system
	 */
	private static final String TAG = "TestDataLogger";

	/**
	 * File handle for upload test data
	 */
	private File upload_test_data_file_;

	/**
	 * File handle for download test data
	 */
	private File download_test_data_file_;

	/**
	 * Singleton implementation instance
	 */
	private static TestDataLogger instance_;

	/**
	 * Maps between the upload log and the CLConnection
	 */
	private HashMap<CLConnection, TestDataLog> upload_begin_log_maps_;

	/**
	 * Maps between the download log and the CLConnection
	 */
	private HashMap<CLConnection, TestDataLog> download_begin_log_maps_;

	/**
	 * the uploaded size so far in bytes
	 */
	private long uploaded_size_ = 0;

	/**
	 * the downloaded size so far in bytes
	 */
	private long downloaded_size_ = 0;

	/**
	 * Initialize function to create objects
	 */
	public void init() {

		upload_begin_log_maps_ = new HashMap<CLConnection, TestDataLog>();
		download_begin_log_maps_ = new HashMap<CLConnection, TestDataLog>();

		// TODO: Below line should call something outside the library
		File dir = new File("/sdcard/" + TempPrefixName);
		if (!dir.exists())
			dir.mkdir();
		try {
			upload_test_data_file_ = new File(dir, "upload.log");
			if (!upload_test_data_file_.exists())
				upload_test_data_file_.createNewFile();

			download_test_data_file_ = new File(dir, "download.log");
			if (!download_test_data_file_.exists())
				download_test_data_file_.createNewFile();

			uploaded_size_ = 0;
			downloaded_size_ = 0;

		} catch (IOException e) {
			BPF.getInstance().getBPFLogger()
					.error(TAG, "TestDataLogger initialization fail");
		}
	}

	/**
	 * Shutdown the instance
	 */
	public void shutdown() {
		instance_ = null;
	}

	/**
	 * Singleton interface
	 * 
	 * @return
	 */
	public static TestDataLogger getInstance() {
		if (instance_ == null) {
			instance_ = new TestDataLogger();

		}

		return instance_;
	}

	/**
	 * enum for logging data type
	 */
	static enum test_data_type_t {
		UPLOAD, DOWNLOAD
	}

	/**
	 * Log the moment a bundle downloading started
	 * 
	 * @param con
	 */
	public void log_bundle_download_begin(CLConnection con) {
		download_begin_log_maps_.put(con, get_current_value());
	}

	/**
	 * Log the moment a bundle downloading ended. This will compare with the
	 * previous start download logging and write the result.
	 * 
	 * @param con
	 * @param incoming
	 */
	public void log_bundle_download_end(CLConnection con,
			IncomingBundle incoming) {
		TestDataLog download_begin_dat = download_begin_log_maps_.get(con);

		compare_and_write_result(incoming.bundle(), incoming.total_length(),
				download_begin_dat, get_current_value(),
				test_data_type_t.DOWNLOAD);
	}

	/**
	 * Log the moment a bundle uploading started
	 * 
	 * @param con
	 * @param bundle
	 */
	public void log_bundle_upload_begin(CLConnection con, Bundle bundle) {
		upload_begin_log_maps_.put(con, get_current_value());
	}

	/**
	 * Log the moment a bundle uploading ended. This will compare with the
	 * previous start upload logging and write the result.
	 * 
	 * @param con
	 * @param inflight
	 */
	public void log_bundle_upload_end(CLConnection con, InFlightBundle inflight) {
		TestDataLog upload_begin_data = upload_begin_log_maps_.get(con);
		// uploaded_size_ += inflight.total_length();
		compare_and_write_result(inflight.bundle(), inflight.total_length(),
				upload_begin_data, get_current_value(), test_data_type_t.UPLOAD);
	}

	/**
	 * Get current status of the system as TestDataLog object
	 * 
	 * @return current status of the system as TestDataLog
	 */
	public TestDataLog get_current_value() {
		TestDataLog testDataLog = new TestDataLog();
		testDataLog.time_milliseconds = System.currentTimeMillis();
		return testDataLog;
	}

	/**
	 * Compare the starting and ending TestDataLog and write to the logging file
	 * 
	 * @param bundle
	 * @param total_length
	 * @param begin
	 * @param end
	 * @param type
	 */
	private void compare_and_write_result(Bundle bundle, long total_length,
			TestDataLog begin, TestDataLog end, test_data_type_t type) {
		// compare
		long time_used = end.time_milliseconds - begin.time_milliseconds;

		Date current_date = new Date();
		Date start_date = new Date(begin.time_milliseconds);
		Date end_date = new Date(end.time_milliseconds);
		// write result

		String record_data = String.format(
				// Time written, bundle id , size ( bytes ), time start(
				// readable ), time end ( readable ), time used ( millisecond ),
				"%s,%d,%d,%s,%s,%d", current_date.toString(),
				bundle.bundleid(), total_length, start_date.toString(),
				end_date.toString(), time_used);
		try {

			if (type == test_data_type_t.UPLOAD) {

				FileWriter upload_file_handle_ = new FileWriter(
						upload_test_data_file_, true);

				upload_file_handle_.append(record_data + "\n");
				upload_file_handle_.flush();
				upload_file_handle_.close();
			}

			if (type == test_data_type_t.DOWNLOAD) {
				FileWriter download_file_handle_ = new FileWriter(
						download_test_data_file_, true);
				download_file_handle_.append(record_data + "\n");
				download_file_handle_.flush();
				download_file_handle_.close();
			}

		} catch (IOException e) {
			BPF.getInstance().getBPFLogger()
					.error(TAG, "Error writting test data files");
		}
	}

	/**
	 * Class representing the data for logging
	 */
	static class TestDataLog {
		long time_milliseconds;

		TestDataLog() {
			time_milliseconds = -1;
		}
	}

	/**
	 * Getter for the uploaded size
	 * 
	 * @return the uploaded_size_
	 */
	public synchronized long uploaded_size() {
		return uploaded_size_;
	}

	/**
	 * Setter for the uploaded size
	 * 
	 * @param uploadedSize
	 *            the uploaded_size_ to set
	 */
	public synchronized void set_uploaded_size(long uploaded_size) {
		uploaded_size_ = uploaded_size;
	}

	/**
	 * Getter for the downloaded size
	 * 
	 * @return the downloaded_size_
	 */
	public synchronized long downloaded_size() {
		return downloaded_size_;
	}

	/**
	 * Setter for the downloaded size
	 * 
	 * @param downloadedSize
	 *            the downloaded_size_ to set
	 */
	public synchronized void set_downloaded_size(long downloaded_size) {
		downloaded_size_ = downloaded_size;
	}

}
