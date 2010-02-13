/***
	Copyright (c) 2008-2009 CommonsWare, LLC
	Portions (c) 2009 Google, Inc.
	
	Licensed under the Apache License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may obtain
	a copy of the License at
		http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/		

package com.commonsware.cwac.endless;

import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.commonsware.cwac.adapter.AdapterWrapper;

/**
 * Adapter that assists another adapter in appearing endless.
 * For example, this could be used for an adapter being
 * filled by a set of Web service calls, where each call returns
 * a "page" of data.
 *
 * Subclasses need to be able to return, via getPendingView()
 * a row that can serve as both a placeholder while more data
 * is being appended, and then later an actual row in the
 * result set. You might accomplish this via two widgets
 * in a FrameLayout, only one of which is visible (e.g., an
 * ImageView doing a rotation animation while loading new
 * data, then your regular row).
 *
 * Subclasses will be handed that row View back on rebindPendingView()
 * so they can flip back to normal mode.
 *
 * The actual logic for loading new data should be done in
 * appendInBackground(). This method, as the name suggests,
 * is run in a background thread. It should return true if
 * there might be more data, false otherwise.
 *
 * If your situation is such that you will not know if there
 * is more data until you do some work (e.g., make another
 * Web service call), it is up to you to do something useful
 * with that row returned by getPendingView() to let the user
 * know you are out of data, plus return false from that final
 * call to appendInBackground().
 */
abstract public class EndlessAdapter extends AdapterWrapper {
	abstract protected View getPendingView(ViewGroup parent);
	abstract protected void rebindPendingView(int position,
																						View convertView);
	abstract protected boolean cacheInBackground();
	abstract protected void appendCachedData();
	
	private View pendingView=null;
	private int pendingPosition=-1;
	private AtomicBoolean keepOnAppending=new AtomicBoolean(true);

	/**
		* Constructor wrapping a supplied ListAdapter
    */
	public EndlessAdapter(ListAdapter wrapped) {
		super(wrapped);
	}

	/**
		* How many items are in the data set represented by this
		* Adapter.
    */
	@Override
	public int getCount() {
		if (keepOnAppending.get()) {
			return(super.getCount()+1);		// one more for "pending"
		}
		
		return(super.getCount());
	}

	/**
		* Get a View that displays the data at the specified
		* position in the data set. In this case, if we are at
		* the end of the list and we are still in append mode,
		* we ask for a pending view and return it, plus kick
		* off the background task to append more data to the
		* wrapped adapter.
		* @param position Position of the item whose data we want
		* @param convertView View to recycle, if not null
		* @param parent ViewGroup containing the returned View
    */
	@Override
	public View getView(int position, View convertView,
											ViewGroup parent) {
		if (position==super.getCount() &&
				keepOnAppending.get()) {
			if (pendingView==null) {
				pendingView=getPendingView(parent);
				pendingPosition=position;
				
				new AppendTask().execute();
			}
			
			return(pendingView);
		}
		
		return(super.getView(position, convertView, parent));
	}
	
	/**
	 * A background task that will be run when there is a need
	 * to append more data. Mostly, this code delegates to the
	 * subclass, to append the data in the background thread and
	 * rebind the pending view once that is done.
	 */
	class AppendTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			keepOnAppending.set(cacheInBackground());
			
			return(null);
		}
		
		@Override
		protected void onPostExecute(Void unused) {
			appendCachedData();
			rebindPendingView(pendingPosition, pendingView);
			notifyDataSetChanged();
			pendingView=null;
			pendingPosition=-1;
		}
	}
}