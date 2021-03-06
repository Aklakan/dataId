package aksw.dataid.datahub.jsonobjects;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatahubResponse <T>
{
    public boolean success;
    public T result;	
    public DatahubException error;
    
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public T getResult() {
		return result;
	}
	public void setResult(T result) {
		this.result = result;
	}
	public DatahubException getError() {
		return error;
	}
	public void setError(DatahubException error) {
		this.error = error;
	}
}
