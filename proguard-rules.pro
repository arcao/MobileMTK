#prevent severe obfuscation
-keep,allowshrinking,allowoptimization class * { <methods>; } 

-keepclasseswithmembernames,allowshrinking,allowoptimization class * {
    native <methods>;
}

-keepclasseswithmembers,allowshrinking,allowoptimization class * {
    public <init>(android.content.Context);
    public <init>(android.content.Context,android.util.AttributeSet);
    public <init>(android.content.Context,android.util.AttributeSet,int);
}

-keepclassmembers,allowoptimization class * {
    public <init>(android.content.Context);
    public <init>(android.content.Context,android.util.AttributeSet);
    public <init>(android.content.Context,android.util.AttributeSet,int);
}

#ButterKnife
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }
-keepnames class * { @butterknife.InjectView *;}
#

#Dagger
-keepclassmembers,allowoptimization class * {
    @javax.inject.* *;
    @dagger.* *;
    <init>();
}
-keep class **$$ModuleAdapter
-keep class **$$InjectAdapter
-keep class **$$StaticInjection
-dontwarn dagger.internal.codegen.**
-dontwarn com.squareup.javawriter.**
#

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable #needed
-keepattributes Signature # Needed by google-api-client #to make XStream work with obfuscation?
-keepattributes EnclosingMethod #required?
-keepattributes InnerClasses #required?

-keepattributes Exceptions # can be removed?
-keepattributes Deprecated # can be removed?
-keepattributes Synthetic # can be removed?

-keepattributes *Annotation*
