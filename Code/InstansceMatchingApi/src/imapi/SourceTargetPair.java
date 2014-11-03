/*
 * Copyright 2014 Your Name <Elias Tzortzakakis at tzortzak@ics.forth.gr>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package imapi;

/**
 *
 * @author tzortzak
 */

import java.util.Comparator;

class SourceTargetPair implements Comparator<SourceTargetPair>, Comparable<SourceTargetPair>{

	private SourceInstancePair sourceInstance;
	private SourceInstancePair targetInstance;

	public SourceTargetPair(SourceInstancePair srcInstance, SourceInstancePair trgtInstance) {
		this.sourceInstance = srcInstance;
		this.targetInstance = trgtInstance;
	}
	
	public SourceInstancePair getSourceInstance(){
		return this.sourceInstance;
	}
	
	public SourceInstancePair getTargetInstance(){
		return this.targetInstance;
	}
	
	@Override
	public boolean equals(Object obj){
		
		if(obj==null || (obj instanceof SourceTargetPair) ==false){
			return false;
		}
		
		SourceTargetPair otherObj = (SourceTargetPair) obj;
		
		boolean result =this.getSourceInstance().getSourceName().equals(otherObj.getSourceInstance().getSourceName());
		if(!result){
			return false;
		}
		result =this.getSourceInstance().getInstanceUri().equals(otherObj.getSourceInstance().getInstanceUri());
		if(!result){
			return false;
		}
		
		result =this.getTargetInstance().getSourceName().equals(otherObj.getTargetInstance().getSourceName());
		if(!result){
			return false;
		}
		
		result =this.getTargetInstance().getInstanceUri().equals(otherObj.getTargetInstance().getInstanceUri());
		if(!result){
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode(){
		return (this.sourceInstance.getSourceName()+this.sourceInstance.getInstanceUri()+this.targetInstance.getSourceName()+this.targetInstance.getInstanceUri()).hashCode();
	}

	@Override
	public int compareTo(SourceTargetPair o) {
		
		return this.compare(this, o);
	}

	@Override
	public int compare(SourceTargetPair o1, SourceTargetPair o2) {
		
		if(o1==null)
		{
			if(o2==null){
				return 0;
			}
			else{
				return -1;
			}
		}
		
		if(o2==null)
		{
			if(o1!=null){				
				return 1;
			}			
		}
		
		int result =o1.getSourceInstance().getSourceName().compareTo(o2.getSourceInstance().getSourceName());
		if(result!=0){
			return result;
		}
		result =o1.getSourceInstance().getInstanceUri().compareTo(o2.getSourceInstance().getInstanceUri());
		if(result!=0){
			return result;
		}
		
		result =o1.getTargetInstance().getSourceName().compareTo(o2.getTargetInstance().getSourceName());
		if(result!=0){
			return result;
		}
		
		result =o1.getTargetInstance().getInstanceUri().compareTo(o2.getTargetInstance().getInstanceUri());
		if(result!=0){
			return result;
		}
		
		return 0;
		
		
	}

}
