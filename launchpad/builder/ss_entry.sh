BUNDLE_NAME=`basename $3`
mkdir -p target/tmp/$BUNDLE_NAME
pushd target/tmp/$BUNDLE_NAME
jar xvf $3 META-INF/MANIFEST.MF
BSN=`grep Bundle-SymbolicName META-INF/MANIFEST.MF | awk '{print $2}' | tr -d \\\\r `

if [ "org.apache.sling.extensions.webconsolesecuritypro" == $BSN ]; then
  BSN=org.apache.sling.extensions.webconsolesecurityprovider
fi

BVER=`grep Bundle-Version META-INF/MANIFEST.MF | awk '{print $2}' | tr -d \\\\r `
popd

cp $3 $1
echo "  $BSN;version=\"[$BVER,$BVER]\";start-order:=$2," >> $1/OSGI-INF/SUBSYSTEM.MF

