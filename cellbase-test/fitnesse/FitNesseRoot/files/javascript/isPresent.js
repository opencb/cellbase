function isPresent () {
	var jsonbody = arguments[0];
	var valueToMatch = arguments[1];
	var JSONString = arguments[2];

	for(count = 0; count < jsonbody.response[0].numResults; count++){
	var res = jsonbody.response[0].result[count];
	if (valueToMatch == res[JSONString]){
		return true;
	    }
	}
	return false;
}
