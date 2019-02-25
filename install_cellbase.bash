qseqdnamatch=`expr match "$(pwd)" '.*\(cellbase\)'`
if [[ $qseqdnamatch = "cellbase" ]]
then
    echo "Already in cellbase folder."
    git pull origin master
else
    #Now, this script, when executed from outside the qiaseq folder, it downloads the qiaseq repository and then executes the script 'install_qiaseq_dna.bash'.
    #This allows that the installer be updated and not to have to provide the updated installer script
    echo "Not in cellbase folder."
    if [[ -d "cellbase" ]]
    then
        cd cellbase && ./install_cellbase.bash $@
    elif [[ -e "cellbase" ]]
    then
        echo "File cellbase exists but it is not a directory, thus we can not create a directory with that path tho hold the software reposotory. \
        See if it is safe to delete or move it, and then execute again this script."
    else
        git clone https://github.com/Lucioric2000/cellbase
        cd cellbase && ./install_cellbase.bash $@
    fi
    exit
fi

# Install apache Maven
wget https://www-us.apache.org/dist/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.tar.gz
tar xzvf apache-maven-3.6.0-bin.tar.gz


cp mvn_default_settings.xml ~.m2/settings.xml
mvn clean install -DskipTests