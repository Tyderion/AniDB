/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package aniAdd;

import aniAdd.misc.Misc;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
/**
 *
 * @author Arokh
 */
public interface Communication {
	//private ArrayList<ComListener> listeners;

    /*protected void ComFire(ComEvent comEvent){
        for (ComListener listener : listeners) {
            listener.EventHandler(comEvent);
        }
    }*/
	//public void AddComListener(ComListener comListener){ listeners.add(comListener); }
	//public void RemoveComListener(ComListener comListener){ listeners.remove(comListener); }
    void AddComListener(ComListener comListener);
    void RemoveComListener(ComListener comListener);

	public static interface ComListener extends EventListener{
		void EventHandler(ComEvent comEvent);
	}

	public static class ComEvent extends EventObject{
        long createdOn;
		eType type;
		ArrayList<Object> params;

		public ComEvent(Object source, eType type, ArrayList<Object> params){
			this(source, type, params.toArray());
		}
		public ComEvent(Object source, eType type, Object... params){
			this(source, type);

			this.params = new ArrayList<Object>();
			for (Object param : params) {
				this.params.add(param);
			}
		}
		public ComEvent(Object source, eType type){
			this(source);
			this.type = type;
		}
		private ComEvent(Object source) { 
            super(source);
            createdOn = System.currentTimeMillis();
        }

		public eType Type(){ return type; }
		public Object Params(int i) { return params.get(i); }
        public int ParamCount(){ return params.size(); }

        public String toString(){
            String str;
            str = Misc.longToTime(createdOn) + ": " + (getSource() instanceof Module?(((Module)getSource()).ModuleName() + " " + Type()):"");
            for (int i=0; i<ParamCount(); i++) {
                str += " " + Params(i).toString();
            }
            return str;
        }
        
		public enum eType{Debug, Information, Manipulation, Warning, Error, Fatal}
	}
}