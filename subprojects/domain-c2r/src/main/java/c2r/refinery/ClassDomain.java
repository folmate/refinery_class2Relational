package c2r.refinery;

import atl.research.class_.DataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import tools.refinery.store.dse.modification.ModificationAdapter;
import tools.refinery.store.model.Interpretation;
import tools.refinery.store.model.Model;
import tools.refinery.store.model.ModelStoreBuilder;
import tools.refinery.store.query.view.AnySymbolView;
import tools.refinery.store.query.view.FunctionView;
import tools.refinery.store.query.view.KeyOnlyView;
import tools.refinery.store.representation.Symbol;
import tools.refinery.store.tuple.Tuple;

import java.util.HashMap;
import java.util.Map;

public class ClassDomain {
	public static final Symbol<String> class_name;
	public static final FunctionView<String> class_nameView;
	public final Interpretation<String> name;
	public static final Symbol<Boolean> class_Class;
	public static final AnySymbolView class_ClassView;
	public final Interpretation<Boolean> Class;
	public static final Symbol<Boolean> class_DataType;
	public static final AnySymbolView class_DataTypeView;
	public final Interpretation<Boolean> DataType;
	public static final Symbol<Boolean> class_Attribute;
	public static final AnySymbolView class_AttributeView;
	public final Interpretation<Boolean> Attribute;
	public static final Symbol<Boolean> class_att;
	public static final AnySymbolView class_attView;
	public final Interpretation<Boolean> att;
	public static final Symbol<Boolean> class_type;
	public static final AnySymbolView class_typeView;
	public final Interpretation<Boolean> type;
	public static final Symbol<Boolean> class_multiValued;
	public static final AnySymbolView class_multivaluedView;
	public final Interpretation<Boolean> multiValued;
	static {
		class_name = Symbol.of("name:String@class.NamedElt",1, String.class,null);
		class_nameView = new FunctionView<>(class_name);

		class_DataType = Symbol.of("class.DataType",1);
		class_DataTypeView = new KeyOnlyView<>(class_DataType);

		class_Class = Symbol.of("class.Class",1);
		class_ClassView = new KeyOnlyView<>(class_Class);

		class_Attribute = Symbol.of("class.Attribute",1);
		class_AttributeView = new KeyOnlyView<>(class_Attribute);

		class_att = Symbol.of("att:class.Attribute@class.Class",2);
		class_attView = new KeyOnlyView<>(class_att);

		class_type = Symbol.of("type:class.Classifier@class.Attribute",2);
		class_typeView = new KeyOnlyView<>(class_type);


		class_multiValued = Symbol.of("multiValued:Boolean@class.Attribute",1);
		class_multivaluedView = new KeyOnlyView<>(class_multiValued);
	}
	private final Map<EObject,Tuple> trace = new HashMap<>();

	public ClassDomain(Model model){
		this.model = model;
		name = model.getInterpretation(class_name);
		Class = model.getInterpretation(class_Class);
		DataType = model.getInterpretation(class_DataType);
		Attribute = model.getInterpretation(class_Attribute);
		att = model.getInterpretation(class_att);
		type = model.getInterpretation(class_type);
		multiValued = model.getInterpretation(class_multiValued);
	}
	public String toString() {
		var b = new StringBuilder();
		b.append("Class Domain\n");


		var dtypes = DataType.getAll();
		while(dtypes.move()){
			b.append("\tDataType").append(dtypes.getKey())
					.append(", name=\"").append(name.get(dtypes.getKey()))
					.append("\"\n");
		}

		var classes = model.getInterpretation(class_Class).getAll();
		while(classes.move()) {
			b.append("\tClass").append(classes.getKey())
					.append(", name=\"").append(name.get(classes.getKey()))
					.append("\"\n");
			var attCursor = att.getAll();
			while(attCursor.move()){
				if(attCursor.getKey().get(0)==classes.getKey().get(0)){
					var att = Tuple.of(attCursor.getKey().get(1));
					b.append("\t\tAttribute").append(att)
							.append(", name=\"").append(name.get(att));
					var types = type.getAll();
					while(types.move()){
						if(types.getKey().get(0)==att.get(0)){
							var t = DataType.get(Tuple.of(types.getKey().get(1)));
							b.append(", type=").append(name.get(Tuple.of(types.getKey().get(1))));
						}
					}
					b.append(", multivalued=").append(multiValued.get(att))
							.append("\n");
				}
			}
		}
		return b.toString();
	}
	private final Model model;

	public Tuple tupleOf(Object source){
		return trace.get((EObject) source);
	}

	public ClassDomain(Model model, Resource resource){
		this.model = model;
		name = model.getInterpretation(class_name);
		Class = model.getInterpretation(class_Class);
		DataType = model.getInterpretation(class_DataType);
		Attribute = model.getInterpretation(class_Attribute);
		att = model.getInterpretation(class_att);
		type = model.getInterpretation(class_type);
		multiValued = model.getInterpretation(class_multiValued);

		var mod = model.getAdapter(ModificationAdapter.class);

		resource.getContents().forEach((EObject node) ->{
			if(node instanceof atl.research.class_.Class cls){
				var newclass = createClass();
				trace.put(cls,newclass);
				if(cls.getName()!=null)
					name.put(newclass,cls.getName());

				cls.getAttr().forEach(attr -> {
					var newattr = createAttributeOf(newclass);
					trace.put(attr,newattr);
					if(attr.getMultiValued() != null)
						multiValued.put(newattr, attr.getMultiValued());
					if(attr.getName() != null)
						name.put(newattr,attr.getName());
				});
			}
			if(node instanceof DataType dtype){
				var newtype = createDataType();
				trace.put(dtype,newtype);
				if(dtype.getName()!=null)
					name.put(newtype, dtype.getName());
			}
		});

		trace.forEach((src,tuple)->{
			if(src instanceof atl.research.class_.Attribute attr){
				type.put(Tuple.of(tuple.get(0),trace.get(attr.getType()).get(0)),true );
			}
		});
	}
	public static void build(ModelStoreBuilder builder){
		builder.symbols(/*OBJECTS,*/class_name, class_Class, class_DataType, class_Attribute, class_att, class_type,
				class_multiValued);
	}

	private Tuple createNewObject(){
		return model.getAdapter(ModificationAdapter.class).createObject();
	}
	public Tuple createClass(){
		var id = createNewObject();
		Class.put(id,true);
		return id;
	}

	public Tuple createDataType(){
		var id = createNewObject();
		DataType.put(id,true);
		return id;
	}
	public Tuple createAttributeOf(Tuple cls){
		var id = createNewObject();
		Attribute.put(id,true);
		att.put(Tuple.of(cls.get(0),id.get(0)),true);
		return id;
	}
}
