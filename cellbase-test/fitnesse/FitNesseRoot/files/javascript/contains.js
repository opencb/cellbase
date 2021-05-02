function contains (targetList, subjectList) {
    for (json in subjectList) {
        i = 0;
        while (i < targetList.length && compareJSON(json, targetList[i])) {
            i++;
        }
        // got to the end of the list without finding the JSON
        if (i == targetList.length) {
            return false;
        }
    }
    return true;
}
